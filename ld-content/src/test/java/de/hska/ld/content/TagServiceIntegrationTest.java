package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.AbstractIntegrationTest2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TagServiceIntegrationTest  extends AbstractIntegrationTest2 {

    @Autowired
    TagService tagService;

    public static String TAG_NAME1 = "tagName1";
    public static String TAG_DESCRIPTION1 = "tagDescription1";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testCreateTagWithNameAndDescription() {
        Tag tag = tagService.createTag(TAG_NAME1, TAG_DESCRIPTION1);
        Assert.assertEquals(TAG_NAME1, tag.getName());
        Assert.assertEquals(TAG_DESCRIPTION1, tag.getDescription());
        Assert.assertNotNull(tag.getId());
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testCreateTagWithoutName() {
        Tag tag = tagService.createTag(null, TAG_DESCRIPTION1);
        Assert.assertEquals(TAG_NAME1, tag.getName());
        Assert.assertEquals(TAG_DESCRIPTION1, tag.getDescription());
        Assert.assertNotNull(tag.getId());
    }
}
