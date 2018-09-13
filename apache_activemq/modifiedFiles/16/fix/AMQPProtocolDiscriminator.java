/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.transport.amqp;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.activemq.command.Command;

/**
 * Used to assign the best implementation of a AmqpProtocolConverter to the
 * AmqpTransport based on the AmqpHeader that the client sends us.
 */
public class AMQPProtocolDiscriminator implements IAmqpProtocolConverter {

    final private AmqpTransport transport;

    interface Discriminator {
        boolean matches(AmqpHeader header);

        IAmqpProtocolConverter create(AmqpTransport transport);
    }

    static final private ArrayList<Discriminator> DISCRIMINATORS = new ArrayList<Discriminator>();
    static {
        DISCRIMINATORS.add(new Discriminator() {

            @Override
            public IAmqpProtocolConverter create(AmqpTransport transport) {
                return new AmqpProtocolConverter(transport);
            }

            @Override
            public boolean matches(AmqpHeader header) {
                switch (header.getProtocolId()) {
                    case 0:
                    case 3:
                        if (header.getMajor() == 1 && header.getMinor() == 0 && header.getRevision() == 0) {
                            return true;
                        }
                }
                return false;
            }
        });

    }

    final private ArrayList<Command> pendingCommands = new ArrayList<Command>();

    public AMQPProtocolDiscriminator(AmqpTransport transport) {
        this.transport = transport;
    }

    @Override
    public void onAMQPData(Object command) throws Exception {
        if (command.getClass() == AmqpHeader.class) {
            AmqpHeader header = (AmqpHeader) command;

            Discriminator match = null;
            for (Discriminator discriminator : DISCRIMINATORS) {
                if (discriminator.matches(header)) {
                    match = discriminator;
                }
            }
            // Lets use first in the list if none are a good match.
            if (match == null) {
                match = DISCRIMINATORS.get(0);
            }
            IAmqpProtocolConverter next = match.create(transport);
            transport.setProtocolConverter(next);
            for (Command send : pendingCommands) {
                next.onActiveMQCommand(send);
            }
            pendingCommands.clear();
            next.onAMQPData(command);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onAMQPException(IOException error) {
    }

    @Override
    public void onActiveMQCommand(Command command) throws Exception {
        pendingCommands.add(command);
    }

    @Override
    public void updateTracer() {
    }
}
