package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;

import java.util.Collections;

public class ExternalCsvSearchTest {

    private static final String EXTERNAL_CSV_SEARCH_FORM = "external-csv-search.xml";

    public FormActivityTestRule rule = AdbFormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_CSV_SEARCH_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_CSV_SEARCH_FORM, Collections.singletonList("external-csv-search-produce.csv"), true))
            .around(rule);

    @Test
    public void search_withoutFilter_displaysAllChoices() {
        new FormEntryPage("external-csv-search").assertOnPage()
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertText("Cherimoya")
                .assertText("Carrot");
    }

    @Test
    // Regression: https://github.com/getodk/collect/issues/3132
    public void search_withFilter_showsMatchingChoices() {
        new FormEntryPage("external-csv-search").assertOnPage()
                .swipeToNextQuestion("Produce search")
                .inputText("A")
                .swipeToNextQuestion("Produce")
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Cherimoya")
                .assertText("Carrot")
                .assertTextDoesNotExist("Blueberry")
                .swipeToPreviousQuestion()
                .inputText("B")
                .swipeToNextQuestion("Produce")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertTextDoesNotExist("Artichoke")
                .assertTextDoesNotExist("Apple")
                .assertTextDoesNotExist("Cherimoya")
                .assertTextDoesNotExist("Carrot");
    }
}
