/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.bacnet4j.type.enumerated;

import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.util.queue.ByteQueue;

/**
 * @author Matthew Lohbihler
 */
public class DoorSecuredStatus extends Enumerated {
    private static final long serialVersionUID = 7337105893343734773L;
    public static final DoorSecuredStatus secured = new DoorSecuredStatus(0);
    public static final DoorSecuredStatus unsecured = new DoorSecuredStatus(1);
    public static final DoorSecuredStatus unknown = new DoorSecuredStatus(2);

    public DoorSecuredStatus(int value) {
        super(value);
    }

    public DoorSecuredStatus(ByteQueue queue) {
        super(queue);
    }
}
