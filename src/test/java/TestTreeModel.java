import static org.junit.Assert.assertEquals;

import java.util.SortedSet;

import org.junit.Test;

import com.google.common.collect.Sets;

import fr.onagui.control.MyTreeModel;

public class TestTreeModel {

	@Test
	public void testLanguageSelection() {
		MyTreeModel<String> myTreeModel = new MyTreeModel<String>(new MyOntoContainerTestAdapter<String>() {
			@Override
			public SortedSet<String> getAllLanguageInLabels() {
				return Sets.newTreeSet(Sets.newHashSet("fr", "en"));
			}
		});
		assertEquals("en", myTreeModel.getCurrentShowingLanguage());
	}

	@Test
	public void testLanguageSelectionNoLang() {
		MyTreeModel<String> myTreeModel = new MyTreeModel<String>(new MyOntoContainerTestAdapter<String>() {
			@Override
			public SortedSet<String> getAllLanguageInLabels() {
				return Sets.newTreeSet(Sets.newHashSet());
			}
		});
		assertEquals("", myTreeModel.getCurrentShowingLanguage());
	}
}
