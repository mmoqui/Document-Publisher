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

/**
 * The parameters customizing the publication process.
 * @author mmoquillon
 */
public class PublicationParameters {

    private int threadCount = 100;
    private int publicationCount = 100;
    private int documentCount = 1;
    private String componentId;
    private String topicId = "0";
    private String authorId = "0";

    /**
     * Gets the number of threads to use. Each thread simulate a client publishing a contribution.
     * @return the count of threads to use.
     * @implNote By default 100.
     */
    public int getThreadCount() {
        return threadCount;
    }

    @SuppressWarnings("unused")
    public PublicationParameters setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    /**
     * Gets the number of publications each thread has to be performed sequentially.
     * @return the count of contributions to publish for each thread.
     * @implNote By default 100.
     */
    public int getPublicationCount() {
        return publicationCount;
    }

    @SuppressWarnings("unused")
    public void setPublicationCount(int publicationCount) {
        this.publicationCount = publicationCount;
    }

    /**
     * Gets the identifier of the topic into which the contributions have to be published.
     * @return the unique identifier of a topic. The meaning of the topic is related to the Silverpeas components into
     * which the publication is performed. For example, a folder in a Kmelia or an album in a Gallery.
     * @implNote By default, the main or the root topic.
     */
    public String getTopicId() {
        return topicId;
    }

    @SuppressWarnings("unused")
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    /**
     * Gets the identifier of the component instance into which the contributions have to be published.
     * @return the unique component instance identifier.
     */
    public String getComponentId() {
        return componentId;
    }

    @SuppressWarnings("unused")
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * Gets the unique identifier of the user in Silverpeas in the behalf of whom the
     * publication of the contributions has to be performed.
     * @return the unique identifier of the publication author.
     */
    public String getAuthorId() {
        return authorId;
    }

    @SuppressWarnings("unused")
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    /**
     * Gets the number of documents to attach to each publication.
     * @return the number of documents to attach to each publication.
     */
    public int getDocumentCount() {
        return documentCount;
    }

    public PublicationParameters setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
        return this;
    }
}
