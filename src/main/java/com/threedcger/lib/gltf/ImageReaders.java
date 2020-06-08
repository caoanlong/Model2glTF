package com.threedcger.lib.gltf;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class ImageReaders {
    public static ImageReader findImageReader(ByteBuffer imageData)
            throws IOException
    {
        InputStream inputStream =
                Buffers.createByteBufferInputStream(imageData.slice());
        ImageInputStream imageInputStream =
                ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> imageReaders =
                ImageIO.getImageReaders(imageInputStream);
        if (imageReaders.hasNext())
        {
            ImageReader imageReader = imageReaders.next();
            imageReader.setInput(imageInputStream);
            return imageReader;
        }
        throw new IOException("Could not find ImageReader for image data");
    }

    /**
     * Private constructor to prevent instantiation
     */
    private ImageReaders()
    {
        // Private constructor to prevent instantiation
    }
}
