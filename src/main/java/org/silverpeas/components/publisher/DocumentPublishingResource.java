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

import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.*;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.ManagedThreadPoolException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.SilverpeasWebResource;
import org.silverpeas.core.web.WebResourceUri;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.silverpeas.components.publisher.DocumentPublishingResource.BASE_URI;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;

@WebService
@Path(BASE_URI)
public class DocumentPublishingResource implements SilverpeasWebResource {

    static final String BASE_URI = "publisher/documents";
    static final String FILENAME = "Developing Applications with Smalltalk and CouchDB.pdf";

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest httpServletRequest;
    @Inject
    private KmeliaService kmeliaService;
    @Inject
    private AttachmentService attachmentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PublicationResult publish(final PublicationParameters parameters) {
        String instanceId = parameters.getComponentId();
        if (StringUtil.isNotDefined(instanceId)) {
            throw new BadRequestException("Missing the component instance identifier");
        }

        checkTargetResourceExist(parameters);

        PublicationResult result = new PublicationResult(getClass().getSimpleName());
        SilverpeasComponentInstanceProvider.get()
                .getById(instanceId)
                .ifPresentOrElse(
                        c -> performTask(c, parameters, result),
                        sendBackNotFound(instanceId));

        SilverLogger.getLogger(this)
                .info("{0} PARAMETERS: {1} threads with for each {2} creation(s) of publications with {3} attachment(s)",
                        result.getProcess(),
                        parameters.getThreadCount(),
                        parameters.getPublicationCount(),
                        parameters.getDocumentCount());
        SilverLogger.getLogger(this).info("{0} WHOLE TIME : {1}s", result.getProcess(), result.getTime());
        SilverLogger.getLogger(this).info("{0} TIME MEAN  : {1}ms", result.getProcess(), result.getTimeMean());
        return result;
    }

    private static void checkTargetResourceExist(PublicationParameters parameters) {
        NodePK topicId = new NodePK(parameters.getTopicId(), parameters.getComponentId());
        NodeDetail topic = NodeService.get().getDetail(topicId);
        if (topic == null) {
            throw new NotFoundException("No such topic " + parameters.getTopicId() + " in " +
                    parameters.getComponentId());
        }

        User author = User.getById(parameters.getAuthorId());
        if (author == null) {
            throw new NotFoundException("No such user " + parameters.getAuthorId() + " in Silverpeas");
        }
    }

    private static Runnable sendBackNotFound(String instanceId) {
        return () -> {
            throw new NotFoundException("No such application " + instanceId);
        };
    }

    private void performTask(SilverpeasComponentInstance componentInstance,
                             PublicationParameters parameters,
                             PublicationResult result) {
        List<Runnable> clients = getClients(componentInstance, parameters);
        ManagedThreadPool pool = ManagedThreadPool.getPool();
        try {
            Instant start = Instant.now();
            pool.invokeAndAwaitTermination(clients);
            Instant end = Instant.now();
            result.setTime(Duration.between(start, end).getSeconds());
            result.setTimeMean(Duration.between(start, end).toMillis() /
                    ((long) parameters.getPublicationCount() * parameters.getDocumentCount()
                            * parameters.getThreadCount()));
        } catch (ManagedThreadPoolException e) {
            throw new InternalServerErrorException(e);
        }
    }

    private List<Runnable> getClients(SilverpeasComponentInstance componentInstance,
                                      PublicationParameters parameters) {
        List<Runnable> clients = new ArrayList<>(parameters.getThreadCount());
        for (int i = 0; i < parameters.getThreadCount(); i++) {
            final String id = String.valueOf(i);
            Runnable client = () -> publishDocument(id, componentInstance, parameters);
            clients.add(client);
        }
        return clients;
    }

    private void publishDocument(String threadId,
                                 SilverpeasComponentInstance componentInstance,
                                 PublicationParameters parameters) {
        String instanceId = parameters.getComponentId();
        NodePK topicId = new NodePK(parameters.getTopicId(), instanceId);

        MetaData metaData = readMetaData();
        String title = isDefined(metaData.getTitle()) ? metaData.getTitle() : FILENAME;
        String description = isDefined(metaData.getComments()) ? metaData.getComments() : metaData.getSubject();

        int pubCount = parameters.getPublicationCount();
        int docCount = parameters.getDocumentCount();
        try {
            for (int i = 0; i < pubCount; i++) {
                Date now = new Date();
                PublicationDetail publication = PublicationDetail.builder()
                        .setPk(new PublicationPK(null, instanceId))
                        .created(now, parameters.getAuthorId())
                        .setNameAndDescription(title + " " + threadId + i, description)
                        .setContentPagePath("")
                        .build();
                String pubId = kmeliaService.createPublicationIntoTopic(publication, topicId);
                publication.getPK().setId(pubId);

                byte[] content = readContent();
                boolean versioningActive = getBooleanValue(componentInstance.getParameterValue(VERSION_MODE));
                for (int j = 0; j < docCount; j++) {
                    SimpleAttachment file = SimpleAttachment.builder(publication.getLanguage())
                            .setFilename(FILENAME)
                            .setTitle(title + " " + j)
                            .setDescription(description)
                            .setSize(content.length)
                            .setContentType(metaData.getContentType())
                            .setCreationData(parameters.getAuthorId(), now)
                            .build();

                    SimpleDocument document;
                    SimpleDocumentPK docId = new SimpleDocumentPK(null, instanceId);
                    if (versioningActive) {
                        document = new HistorisedDocument(docId, pubId, 0, file);
                        document.setPublicDocument(true);
                        document.setDocumentType(DocumentType.attachment);
                    } else {
                        document = new SimpleDocument(docId, pubId, 0, false, file);
                    }
                    attachmentService.createAttachment(document, new ByteArrayInputStream(content));
                }

                if (publication.isDraft()) {
                    kmeliaService.draftOutPublicationWithoutNotifications(publication.getPK(), topicId,
                            SilverpeasRole.PUBLISHER.getName());
                }
            }
        } catch (Exception e) {
            SilverLogger.getLogger(this).error(e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    @Override
    public HttpServletRequest getHttpRequest() {
        return httpServletRequest;
    }

    @Override
    public WebResourceUri getUri() {
        return new WebResourceUri(BASE_URI, this.httpServletRequest, this.uriInfo);
    }

    @Override
    public String getComponentId() {
        return null;
    }

    private MetaData readMetaData() {
        try (InputStream content = getClass().getResourceAsStream("/" + FILENAME)) {

            Objects.requireNonNull(content, "The file " + FILENAME + " should be available!");

            MetadataExtractor extractor = MetadataExtractor.get();
            return extractor.extractMetadata(content);
        } catch (NullPointerException | IOException e) {
            SilverLogger.getLogger(this).error(e);
            throw new InternalServerErrorException("Cannot extract metadata from " + FILENAME, e);
        }
    }

    private byte[] readContent() {
        try (InputStream content = getClass().getResourceAsStream("/" + FILENAME)) {

            Objects.requireNonNull(content, "The file " + FILENAME + " should be available!");

            return content.readAllBytes();
        } catch (NullPointerException | IOException e) {
            SilverLogger.getLogger(this).error(e);
            throw new InternalServerErrorException("Cannot read content of " + FILENAME, e);
        }
    }
}
