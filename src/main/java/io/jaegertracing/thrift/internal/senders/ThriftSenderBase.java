/*
 * Copyright (c) 2016-2018, The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jaegertracing.thrift.internal.senders;

import io.jaegertracing.thrift.internal.reporters.protocols.ThriftUdpTransport;
import org.apache.thrift.TBase;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.AutoExpandingBufferWriteTransport;


public abstract class ThriftSenderBase {

    public enum ProtocolType {
        Binary,
        Compact
    }

    public static final int EMIT_BATCH_OVERHEAD = 33;

    protected final TProtocolFactory protocolFactory;
    private TSerializer serializer;
    private final int maxBatchBytes;
    private AutoExpandingBufferWriteTransport memoryTransport;

    public TProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public TSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(TSerializer serializer) {
        this.serializer = serializer;
    }

    public AutoExpandingBufferWriteTransport getMemoryTransport() {
        return memoryTransport;
    }

    public void setMemoryTransport(AutoExpandingBufferWriteTransport memoryTransport) {
        this.memoryTransport = memoryTransport;
    }

    /**
     * @param protocolType  protocol type (compact or binary)
     * @param maxPacketSize if 0 it will use default value {@value ThriftUdpTransport#MAX_PACKET_SIZE}
     */
    public ThriftSenderBase(ProtocolType protocolType, int maxPacketSize) {
        switch (protocolType) {
            case Binary:
                this.protocolFactory = new TBinaryProtocol.Factory();
                break;
            case Compact:
                this.protocolFactory = new TCompactProtocol.Factory();
                break;
            default:
                this.protocolFactory = null;
//        log.error("Unknown thrift protocol type specified: " + protocolType);
                break;
        }

        if (maxPacketSize == 0) {
            maxPacketSize = ThriftUdpTransport.MAX_PACKET_SIZE;
        }

        maxBatchBytes = maxPacketSize - EMIT_BATCH_OVERHEAD;
        TConfiguration tConfiguration = new TConfiguration();
        try {
            memoryTransport = new AutoExpandingBufferWriteTransport(tConfiguration, maxPacketSize, 2);
            serializer = new TSerializer(protocolFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected int getMaxBatchBytes() {
        return maxBatchBytes;
    }

    protected byte[] serialize(TBase<?, ?> thriftBase) throws Exception {
        return serializer.serialize(thriftBase);
    }

    public int getSize(TBase<?, ?> thriftBase) throws Exception {
        memoryTransport.reset();
        thriftBase.write(protocolFactory.getProtocol(memoryTransport));
        return memoryTransport.getLength();
    }

}
