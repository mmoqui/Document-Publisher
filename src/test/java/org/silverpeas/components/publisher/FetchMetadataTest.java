/*
 * Copyright (C) 2000 - 2023 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.publisher;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@EnableSilverTestEnv
class FetchMetadataTest {

    @TestManagedBean
    MetadataExtractor extractor;

    @Test
    void fetch() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/" + DocumentPublishingResource.FILENAME)) {
            assertThat(input, is(notNullValue()));

            MetaData metaData = extractor.extractMetadata(input);
            assertThat(metaData, is(notNullValue()));
            assertThat(metaData.getTitle(), is("Developing applications with Smalltalk and CouchDB"));
            assertThat(metaData.getAuthor(), is("J. Marí Aguirre"));
            assertThat(metaData.getContentType(), is("application/pdf"));
        }

        try(InputStream input = getClass().getResourceAsStream("/" + DocumentPublishingResource.FILENAME)) {
            assertThat(input, is(notNullValue()));

            byte[] content = input.readAllBytes();
            assertThat(content, is(notNullValue()));
            assertThat(content.length, is(greaterThan(1)));
        }
    }
}
