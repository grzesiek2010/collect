package org.odk.collect.android.external;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.formentry.questions.ItemsetQuestionDetails;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.XPathParseTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FastExternalItemsReader {

    private static final String QUOTATION_MARK = "\"";

    private FormEntryPrompt formEntryPrompt;
    private XPathParseTool pathParseTool;
    private ItemsetDbAdapter adapter;
    private FileUtil fileUtil;

    public FastExternalItemsReader(FormEntryPrompt formEntryPrompt, XPathParseTool parseTool, ItemsetDbAdapter adapter, FileUtil fileUtil) {
        this.formEntryPrompt = formEntryPrompt;
        this.pathParseTool = parseTool;
        this.adapter = adapter;
        this.fileUtil = fileUtil;
    }

    public List<SelectChoice> getItems() {
        String nodesetString = getNodesetString();

        List<String> arguments = new ArrayList<>();
        String selectionString = getSelectionStringAndPopulateArguments(getQueryString(nodesetString), arguments);

        FormController formController = Collect.getInstance().getFormController();
        String[] selectionArgs = getSelectionArgs(arguments, nodesetString, formController);

        return selectionArgs == null ? null : getItemsFromDatabase(selectionString, selectionArgs, formController);
    }

    private String getNodesetString() {
        // the format of the query should be something like this:
        // query="instance('cities')/root/item[state=/data/state and county=/data/county]"
        // "query" is what we're using to notify that this is an itemset widget.
        return formEntryPrompt.getQuestion().getAdditionalAttribute(null, "query");
    }

    private String getQueryString(String nodesetStr) {
        // isolate the string between between the [ ] characters
        return nodesetStr.substring(nodesetStr.indexOf('[') + 1, nodesetStr.lastIndexOf(']'));
    }

    private String getSelectionStringAndPopulateArguments(String queryString, List<String> arguments) {
        StringBuilder selectionString = new StringBuilder();
        // add the list name as the first argument, which will always be there
        selectionString.append("list_name=?");

        // check to see if there are any arguments
        if (queryString.indexOf('=') != -1) {
            selectionString.append(" and ");
        }

        // can't just split on 'and' or 'or' because they have different
        // behavior, so loop through and break them off until we don't have any more
        // must include the spaces in indexOf so we don't match words like "land"
        int andIndex;
        int orIndex = -1;

        while ((andIndex = queryString.indexOf(" and ")) != -1 || (orIndex = queryString.indexOf(" or ")) != -1) {
            if (andIndex != -1) {
                String[] pair = queryString
                        .substring(0, andIndex)
                        .split("=");

                if (pair.length == 2) {
                    selectionString
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? and ");

                    arguments
                            .add(pair[1]
                                    .trim());
                }
                // move string forward to after " and "
                queryString = queryString.substring(andIndex + 5, queryString.length());
            } else {
                String subString = queryString.substring(0, orIndex);
                String[] pair = subString.split("=");

                if (pair.length == 2) {
                    selectionString
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? or ");
                    arguments.add(pair[1].trim());
                }
                // move string forward to after " or "
                queryString = queryString.substring(orIndex + 4, queryString.length());
                orIndex = -1;
            }
        }

        // parse the last segment (or only segment if there are no 'and' or 'or' clauses
        String[] pair = queryString.split("=");
        if (pair.length == 2) {
            selectionString
                    .append(QUOTATION_MARK)
                    .append(pair[0].trim())
                    .append(QUOTATION_MARK)
                    .append("=?");
            arguments.add(pair[1].trim());
        }
        return selectionString.toString();
    }

    private String[] getSelectionArgs(List<String> arguments, String nodesetStr, FormController formController) {
        // +1 is for the list_name
        String[] selectionArgs = new String[arguments.size() + 1];

        // parse out the list name, between the ''
        String listName = nodesetStr.substring(nodesetStr.indexOf('\'') + 1, nodesetStr.lastIndexOf('\''));

        selectionArgs[0] = listName; // first argument is always listname

        if (formController == null) {
            Timber.w("Can't instantiate ItemsetWidget with a null FormController.");
            return null;
        }

        // loop through the arguments, evaluate any expressions and build the query string for the DB
        for (int i = 0; i < arguments.size(); i++) {
            XPathExpression xpr;
            try {
                xpr = pathParseTool.parseXPath(arguments.get(i));
            } catch (XPathSyntaxException e) {
                Timber.e(e);
//                TextView error = new TextView(getContext());
//                error.setText(String.format(getContext().getString(R.string.parser_exception), arguments.get(i)));
//                addAnswerView(error);
                break;
            }

            if (xpr != null) {
                FormDef form = formController.getFormDef();
                TreeElement treeElement = form.getMainInstance().resolveReference(
                        formEntryPrompt.getIndex().getReference());
                EvaluationContext ec = new EvaluationContext(form.getEvaluationContext(),
                        treeElement.getRef());
                Object value = xpr.eval(form.getMainInstance(), ec);

                if (value == null) {
                    return null;
                } else {
                    if (value instanceof XPathNodeset) {
                        value = ((XPathNodeset) value).getValAt(0);
                    }
                    selectionArgs[i + 1] = value.toString();
                }
            }
        }
        return selectionArgs;
    }

    private List<SelectChoice> getItemsFromDatabase(String selection, String[] selectionArgs, FormController formController) {
        List<SelectChoice> items = new ArrayList<>();

        File itemsetFile =  fileUtil.getItemsetFile(formController.getMediaFolder().getAbsolutePath());

        if (itemsetFile.exists()) {
            adapter.open();

            // name of the itemset table for this form
            String pathHash = ItemsetDbAdapter.getMd5FromString(itemsetFile.getAbsolutePath());
            try {
                Cursor c = adapter.query(pathHash, selection, selectionArgs);
                if (c != null) {
                    c.move(-1);
                    int index = 0;
                    while (c.moveToNext()) {
                        String label;
                        String val;

                        // try to get the value associated with the label:lang
                        // string if that doen't exist, then just use label
                        String lang = "";
                        if (formController.getLanguages() != null && formController.getLanguages().length > 0) {
                            lang = formController.getLanguage();
                        }

                        // apparently you only need the double quotes in the
                        // column name when creating the column with a : included
                        String labelLang = "label" + "::" + lang;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            label = c.getString(c.getColumnIndex("label"));
                        } else {
                            label = c.getString(c.getColumnIndex(labelLang));
                        }

                        val = c.getString(c.getColumnIndex("name"));
                        SelectChoice selectChoice = new SelectChoice(null, label, val, false);
                        selectChoice.setIndex(index);
                        items.add(selectChoice);
                        index++;
                    }
                    c.close();
                }
            } catch (SQLiteException e) {
                Timber.i(e);
            } finally {
                adapter.close();
            }
        } else {
            //showWarning(getContext().getString(R.string.file_missing, itemsetFile.getAbsolutePath()));
        }
        return items;
    }

    public static Selection getCos(String xmlVal, List<SelectChoice> choices) {
        for (SelectChoice choice : choices) {
            if (choice.selection().xmlValue.equals(xmlVal)) {
                return choice.selection();
            }
        }
        return null;
    }
}
