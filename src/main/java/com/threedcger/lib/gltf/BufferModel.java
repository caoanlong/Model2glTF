package com.threedcger.lib.gltf;

import java.nio.ByteBuffer;

public class BufferModel {
    private String name;

    /**
     * The URI of the buffer data
     */
    private String uri;

    /**
     * The actual data of the buffer
     */
    private ByteBuffer bufferData;

    /**
     * Creates a new instance
     */
    public BufferModel()
    {
        // Default constructor
    }

    /**
     * Set the URI for the buffer data
     *
     * @param uri The URI of the buffer data
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }

    /**
     * Set the data of this buffer
     *
     * @param bufferData The buffer data
     */
    public void setBufferData(ByteBuffer bufferData)
    {
        this.bufferData = bufferData;
    }
    public String getUri()
    {
        return uri;
    }
    public int getByteLength()
    {
        return bufferData.capacity();
    }
    public ByteBuffer getBufferData() { return Buffers.createSlice(bufferData); }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
