/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.AbstractIntegrationTest2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import javax.validation.ConstraintViolationException;

import java.util.List;

import static de.hska.ld.content.ContentFixture.*;

public class TagServiceIntegrationTest  extends AbstractIntegrationTest2 {

    @Autowired
    TagService tagService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
        List<Tag> tagList = tagService.findAll();
        for (Tag tag: tagList) {
            tagService.delete(tag);
        }
    }

    @Test
    public void testGetTagsPage() {
        for (int i = 0; i < 21; i++) {
            tagService.save(newTag());
        }
        Page<Tag> tagPage = tagService.getTagsPage(0, 10, "DESC", "createdAt");
        Assert.assertNotNull(tagPage);
        Assert.assertTrue(tagPage.getTotalElements() == 21);
        Assert.assertTrue(tagPage.getSize() == 10);
    }

    @Test
    public void testCreateTagWithNameAndDescription() {
        Tag tag = tagService.save(newTag(TAG_NAME1, TAG_DESCRIPTION1));
        Assert.assertEquals(TAG_NAME1, tag.getName());
        Assert.assertEquals(TAG_DESCRIPTION1, tag.getDescription());
        Assert.assertNotNull(tag.getId());
    }

    @Test (expected = ConstraintViolationException.class)
    public void testCreateTagWithoutName() {
        Tag tag = tagService.save(newTag(null, TAG_DESCRIPTION1));
        Assert.assertEquals(TAG_NAME1, tag.getName());
        Assert.assertEquals(TAG_DESCRIPTION1, tag.getDescription());
        Assert.assertNotNull(tag.getId());
    }
}
