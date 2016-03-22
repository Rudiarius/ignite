/*
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

package org.apache.ignite.internal.util.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageFactory;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.jetbrains.annotations.Nullable;

/**
 * Parser for direct messages.
 */
public class GridDirectParser implements GridNioParser {
    /** Message metadata key. */
    static final int MSG_META_KEY = GridNioSessionMetaKey.nextUniqueKey();

    /** Reader metadata key. */
    static final int READER_META_KEY = GridNioSessionMetaKey.nextUniqueKey();

    /** */
    private final MessageFactory msgFactory;

    /** */
    private final GridNioMessageReaderFactory readerFactory;

    /**
     * @param msgFactory Message factory.
     * @param readerFactory Message reader factory.
     */
    public GridDirectParser(MessageFactory msgFactory, GridNioMessageReaderFactory readerFactory) {
        assert msgFactory != null;
        assert readerFactory != null;

        this.msgFactory = msgFactory;
        this.readerFactory = readerFactory;
    }

    /** {@inheritDoc} */
    @Nullable @Override public Object decode(GridNioSession ses, ByteBuffer buf)
        throws IOException, IgniteCheckedException {
        MessageReader reader = ses.meta(READER_META_KEY);

        if (reader == null)
            ses.addMeta(READER_META_KEY, reader = readerFactory.reader(ses, msgFactory));

        Message msg = ses.removeMeta(MSG_META_KEY);

        if (msg == null && buf.hasRemaining())
            msg = msgFactory.create(buf.get());

        boolean finished = false;

        if (buf.hasRemaining()) {
            if (reader != null)
                reader.setCurrentReadClass(msg.getClass());

            finished = msg.readFrom(buf, reader);
        }

        if (finished) {
            if (reader != null)
                reader.reset();

            return msg;
        }
        else {
            ses.addMeta(MSG_META_KEY, msg);

            return null;
        }
    }

    /** {@inheritDoc} */
    @Override public ByteBuffer encode(GridNioSession ses, Object msg) throws IOException, IgniteCheckedException {
        // No encoding needed for direct messages.
        throw new UnsupportedEncodingException();
    }
}
