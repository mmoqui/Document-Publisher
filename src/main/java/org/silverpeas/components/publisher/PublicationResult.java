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
 * The result of a publication process.
 * @author mmoquillon
 */
public class PublicationResult {

    private final String process;
    private long time;

    private long timeMean;

    /**
     * Constructs a new result.
     * @param process the name of the publication process.
     */
    public PublicationResult(String process) {
        this.process = process;
    }

    /**
     * Gets the name of the process.
     * @return process name.
     */
    public String getProcess() {
        return process;
    }

    /**
     * Gets time the process took to perform its whole task.
     * @return the number of seconds the process took to perform its task.
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Gets the time mean takes to publish one contribution per thread.
     * @return the time mean of the publishing.
     */
    public long getTimeMean() {
        return timeMean;
    }

    public void setTimeMean(long timeMean) {
        this.timeMean = timeMean;
    }
}
