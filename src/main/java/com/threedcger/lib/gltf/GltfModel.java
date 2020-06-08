/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2016 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.threedcger.lib.gltf;

import com.threedcger.lib.gltf.model.*;
import com.threedcger.utils.IO;
import com.threedcger.utils.MathUtils;
import com.threedcger.utils.Optionals;
import com.threedcger.utils.Utils;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Implementation of a {@link GltfModel}, based on a {@link GlTF glTF 2.0}.<br>
 */
public final class GltfModel {
    private static final Logger logger = Logger.getLogger(GltfModel.class.getName());

    private final GltfAsset gltfAsset;
    private final GlTF gltf;
    private final ByteBuffer binaryData;
    private final List<AccessorModel> accessorModels;
    private final List<BufferModel> bufferModels;
    private final List<BufferViewModel> bufferViewModels;
    private final List<CameraModel> cameraModels;
    private final List<ImageModel> imageModels;
    private final List<MaterialModel> materialModels;
    private final List<MeshModel> meshModels;
    private final List<NodeModel> nodeModels;
    private final List<SceneModel> sceneModels;
    private final List<TextureModel> textureModels;
    private final MaterialModelHandler materialModelHandler;

    public GltfModel(GltfAsset gltfAsset) {
        this.gltfAsset = Objects.requireNonNull(gltfAsset, "The gltfAsset may not be null");
        this.gltf = gltfAsset.getGltf();

        ByteBuffer binaryData = gltfAsset.getBinaryData();
        if (binaryData != null && binaryData.capacity() > 0) {
            this.binaryData = binaryData;
        } else {
            this.binaryData = null;
        }
        
        this.accessorModels = new ArrayList<AccessorModel>();
        this.bufferModels = new ArrayList<BufferModel>();
        this.bufferViewModels = new ArrayList<BufferViewModel>();
        this.cameraModels = new ArrayList<CameraModel>();
        this.imageModels = new ArrayList<ImageModel>();
        this.materialModels = new ArrayList<MaterialModel>();
        this.meshModels = new ArrayList<MeshModel>();
        this.nodeModels = new ArrayList<NodeModel>();
        this.sceneModels = new ArrayList<SceneModel>();
        this.textureModels = new ArrayList<TextureModel>();
        
        this.materialModelHandler = new MaterialModelHandler();
        
        createAccessorModels();
        createBufferModels();
        createBufferViewModels();
        createImageModels();
        createMeshModels();
        createNodeModels();
        createSceneModels();
        createTextureModels();

        initBufferModels();
        initBufferViewModels();
        
        initAccessorModels();
        initImageModels();
        initMeshModels();
        initNodeModels();
        initSceneModels();
        initTextureModels();
        
        instantiateCameraModels();
        instantiateMaterialModels();
    }
    
    
    
    /**
     * Create the {@link AccessorModel} instances
     */
    private void createAccessorModels() {
        List<Accessor> accessors = Optionals.of(gltf.getAccessors());
        for (int i = 0; i < accessors.size(); i++) {
            Accessor accessor = accessors.get(i);
            Integer componentType = accessor.getComponentType();
            Integer count = accessor.getCount();
            ElementType elementType = ElementType.forString(accessor.getType());
            AccessorModel accessorModel =  new AccessorModel(
                componentType, count, elementType);
            accessorModels.add(accessorModel);
        }
    }
    
    /**
     * Create the {@link BufferModel} instances
     */
    private void createBufferModels() {
        List<Buffer> buffers = Optionals.of(gltf.getBuffers());
        for (int i = 0; i < buffers.size(); i++) {
            Buffer buffer = buffers.get(i);
            BufferModel bufferModel = new BufferModel();
            bufferModel.setUri(buffer.getUri());
            bufferModels.add(bufferModel);
        }
    }
    
    /**
     * Create the {@link BufferViewModel} instances
     */
    private void createBufferViewModels() {
        List<BufferView> bufferViews = Optionals.of(gltf.getBufferViews());
        for (int i = 0; i < bufferViews.size(); i++) {
            BufferView bufferView = bufferViews.get(i);
            BufferViewModel bufferViewModel = 
                createBufferViewModel(bufferView);
            bufferViewModels.add(bufferViewModel);
        }
    }

    /**
     * Create a {@link BufferViewModel} for the given {@link BufferView}
     * 
     * @param bufferView The {@link BufferView}
     * @return The {@link BufferViewModel}
     */
    private static BufferViewModel createBufferViewModel(BufferView bufferView) {
        int byteOffset = Optionals.of(bufferView.getByteOffset(), 0);
        int byteLength = bufferView.getByteLength();
        Integer byteStride = bufferView.getByteStride();
        Integer target = bufferView.getTarget();
        BufferViewModel bufferViewModel = new BufferViewModel(target);
        bufferViewModel.setByteOffset(byteOffset);
        bufferViewModel.setByteLength(byteLength);
        bufferViewModel.setByteStride(byteStride);
        return bufferViewModel;
    }
    
    /**
     * Create the {@link ImageModel} instances
     */
    private void createImageModels() {
        List<Image> images = Optionals.of(gltf.getImages());
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            String mimeType = image.getMimeType();
            ImageModel imageModel = new ImageModel(mimeType, null);
            String uri = image.getUri();
            imageModel.setUri(uri);
            imageModels.add(imageModel);
        }
    }
    
    /**
     * Create the {@link MeshModel} instances
     */
    private void createMeshModels() {
        List<Mesh> meshes = Optionals.of(gltf.getMeshes());
        for (int i = 0; i < meshes.size(); i++) {
            meshModels.add(new MeshModel());
        }
    }

    /**
     * Create the {@link NodeModel} instances
     */
    private void createNodeModels() {
        List<Node> nodes = Optionals.of(gltf.getNodes());
        for (int i = 0; i < nodes.size(); i++) {
            nodeModels.add(new NodeModel());
        }
    }

    /**
     * Create the {@link SceneModel} instances
     */
    private void createSceneModels() {
        List<Scene> scenes = Optionals.of(gltf.getScenes());
        for (int i = 0; i < scenes.size(); i++) {
            sceneModels.add(new SceneModel());
        }
    }

    /**
     * Create the {@link TextureModel} instances
     */
    private void createTextureModels() {
        List<Texture> textures = Optionals.of(gltf.getTextures());
        List<Sampler> samplers = Optionals.of(gltf.getSamplers());
        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            Integer samplerIndex = texture.getSampler();
            
            Integer magFilter = GltfConstants.GL_LINEAR;
            Integer minFilter = GltfConstants.GL_LINEAR;
            int wrapS = GltfConstants.GL_REPEAT;
            int wrapT = GltfConstants.GL_REPEAT;
            
            if (samplerIndex != null) {
                Sampler sampler = samplers.get(samplerIndex);
                magFilter = sampler.getMagFilter();
                minFilter = sampler.getMinFilter();
                wrapS = Optionals.of(sampler.getWrapS(), sampler.defaultWrapS());
                wrapT = Optionals.of(sampler.getWrapT(), sampler.defaultWrapT());
            }
            
            textureModels.add(new TextureModel(magFilter, minFilter, wrapS, wrapT));
        }
    }
    
    /**
     * Initialize the {@link AccessorModel} instances
     */
    private void initAccessorModels() {
        List<Accessor> accessors = Optionals.of(gltf.getAccessors());
        for (int i = 0; i < accessors.size(); i++) {
            Accessor accessor = accessors.get(i);
            AccessorModel accessorModel = accessorModels.get(i);
            
            int byteOffset = Optionals.of(accessor.getByteOffset(), 0);
            accessorModel.setByteOffset(byteOffset);

            AccessorSparse accessorSparse = accessor.getSparse();
            if (accessorSparse == null) {
                initDenseAccessorModel(i, accessor, accessorModel);
            } else {
                initSparseAccessorModel(i, accessor, accessorModel);
            }
        }
    }


    /**
     * Initialize the {@link AccessorModel} by setting its 
     * {@link AccessorModel#getBufferViewModel() buffer view model}
     * for the case that the accessor is dense (i.e. not sparse)
     * 
     * @param accessorIndex The accessor index. Only used for constructing
     * the URI string of buffers that may have to be created internally 
     * @param accessor The {@link Accessor}
     * @param accessorModel The {@link AccessorModel}
     */
    private void initDenseAccessorModel(int accessorIndex, Accessor accessor, AccessorModel accessorModel) {
        Integer bufferViewIndex = accessor.getBufferView();
        if (bufferViewIndex != null) {
            // When there is a BufferView referenced from the accessor, then 
            // the corresponding BufferViewModel may be assigned directly
            BufferViewModel bufferViewModel = bufferViewModels.get(bufferViewIndex);
            accessorModel.setBufferViewModel(bufferViewModel);
        } else {
            // When there is no BufferView referenced from the accessor,
            // then a NEW BufferViewModel (and Buffer) have to be created
            int count = accessorModel.getCount();
            int elementSizeInBytes = accessorModel.getElementSizeInBytes();
            int byteLength = elementSizeInBytes * count;
            ByteBuffer bufferData = Buffers.create(byteLength);
            String uriString = "buffer_for_accessor" + accessorIndex + ".bin";
            BufferViewModel bufferViewModel = createBufferViewModel(uriString, bufferData);
            accessorModel.setBufferViewModel(bufferViewModel);
        }
        
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel(); 
        Integer byteStride = bufferViewModel.getByteStride();
        if (byteStride == null) {
            accessorModel.setByteStride(accessorModel.getElementSizeInBytes());
        } else {
            accessorModel.setByteStride(byteStride);
        }
    }
    
    
    /**
     * Initialize the given {@link AccessorModel} by setting its 
     * {@link AccessorModel#getBufferViewModel() buffer view model}
     * for the case that the accessor is sparse. 
     * 
     * @param accessorIndex The accessor index. Only used for constructing
     * the URI string of buffers that may have to be created internally 
     * @param accessor The {@link Accessor}
     * @param accessorModel The {@link AccessorModel}
     */
    private void initSparseAccessorModel(int accessorIndex, Accessor accessor, AccessorModel accessorModel) {
        // When the (sparse!) Accessor already refers to a BufferView,
        // then this BufferView has to be replaced with a new one,
        // to which the data substitution will be applied 
        int count = accessorModel.getCount();
        int elementSizeInBytes = accessorModel.getElementSizeInBytes();
        int byteLength = elementSizeInBytes * count;
        ByteBuffer bufferData = Buffers.create(byteLength);
        String uriString = "buffer_for_accessor" + accessorIndex + ".bin";
        BufferViewModel denseBufferViewModel = createBufferViewModel(uriString, bufferData);
        accessorModel.setBufferViewModel(denseBufferViewModel);
        accessorModel.setByteOffset(0);
        
        Integer bufferViewIndex = accessor.getBufferView();
        if (bufferViewIndex != null) {
            // If the accessor refers to a BufferView, then the corresponding
            // data serves as the basis for the initialization of the values, 
            // before the sparse substitution is applied
            Consumer<ByteBuffer> sparseSubstitutionCallback = denseByteBuffer -> {
                logger.fine("Substituting sparse accessor data," + " based on existing buffer view");
                
                BufferViewModel baseBufferViewModel = bufferViewModels.get(bufferViewIndex);
                ByteBuffer baseBufferViewData = baseBufferViewModel.getBufferViewData();
                AccessorData baseAccessorData = AccessorDatas.create(accessorModel, baseBufferViewData);
                AccessorData denseAccessorData = AccessorDatas.create(accessorModel, bufferData);
                substituteSparseAccessorData(accessor, accessorModel, denseAccessorData, baseAccessorData);
            };
            denseBufferViewModel.setSparseSubstitutionCallback(sparseSubstitutionCallback);
        } else {
            // When the sparse accessor does not yet refer to a BufferView,
            // then a new one is created, 
            Consumer<ByteBuffer> sparseSubstitutionCallback = denseByteBuffer -> 
            {
                logger.fine("Substituting sparse accessor data, "
                    + "without an existing buffer view");
                
                AccessorData denseAccessorData = 
                    AccessorDatas.create(accessorModel, bufferData);
                substituteSparseAccessorData(accessor, accessorModel, 
                    denseAccessorData, null); 
            };
            denseBufferViewModel.setSparseSubstitutionCallback(sparseSubstitutionCallback);
        }
    }
    
    /**
     * Create a new {@link BufferViewModel} with an associated 
     * {@link BufferModel} that serves as the basis for a sparse accessor, or 
     * an accessor that does not refer to a {@link BufferView})
     * 
     * @param uriString The URI string that will be assigned to the 
     * {@link BufferModel} that is created internally. This string 
     * is not strictly required, but helpful for debugging, at least
     * @param bufferData The buffer data
     * @return The new {@link BufferViewModel}
     */
    private static BufferViewModel createBufferViewModel(String uriString, ByteBuffer bufferData) {
        BufferModel bufferModel = new BufferModel();
        bufferModel.setUri(uriString);
        bufferModel.setBufferData(bufferData);

        BufferViewModel bufferViewModel = new BufferViewModel(null);
        bufferViewModel.setByteOffset(0);
        bufferViewModel.setByteLength(bufferData.capacity());
        bufferViewModel.setBufferModel(bufferModel);
        
        return bufferViewModel;
    }
    
    /**
     * Substitute the sparse accessor data in the given dense 
     * {@link AccessorData} for the given {@link AccessorModel}
     * based on the sparse accessor data that is defined in the given 
     * {@link Accessor}.
     * 
     * @param accessor The {@link Accessor}
     * @param accessorModel The {@link AccessorModel}
     * @param denseAccessorData The dense {@link AccessorData}
     * @param baseAccessorData The optional {@link AccessorData} that contains 
     * the base data. If this is not <code>null</code>, then it will be used 
     * to initialize the {@link AccessorData}, before the sparse data 
     * substitution takes place
     */
    private void substituteSparseAccessorData(
        Accessor accessor, AccessorModel accessorModel, 
        AccessorData denseAccessorData, AccessorData baseAccessorData) {
        AccessorSparse accessorSparse = accessor.getSparse();
        int count = accessorSparse.getCount();
        
        AccessorSparseIndices accessorSparseIndices = accessorSparse.getIndices();
        AccessorData sparseIndicesAccessorData = 
            createSparseIndicesAccessorData(accessorSparseIndices, count);
        
        AccessorSparseValues accessorSparseValues = accessorSparse.getValues();
        ElementType elementType = accessorModel.getElementType();
        AccessorData sparseValuesAccessorData =
            createSparseValuesAccessorData(accessorSparseValues, 
                accessorModel.getComponentType(),
                elementType.getNumComponents(), count);
     
        AccessorSparseUtils.substituteAccessorData(
            denseAccessorData, 
            baseAccessorData, 
            sparseIndicesAccessorData, 
            sparseValuesAccessorData);
    }
    
    
    /**
     * Create the {@link AccessorData} for the given 
     * {@link AccessorSparseIndices}
     * 
     * @param accessorSparseIndices The {@link AccessorSparseIndices}
     * @param count The count from the {@link AccessorSparse} 
     * @return The {@link AccessorData}
     */
    private AccessorData createSparseIndicesAccessorData(
        AccessorSparseIndices accessorSparseIndices, int count) {
        Integer componentType = accessorSparseIndices.getComponentType();
        Integer bufferViewIndex = accessorSparseIndices.getBufferView();
        BufferViewModel bufferViewModel = bufferViewModels.get(bufferViewIndex);
        ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
        int byteOffset = Optionals.of(accessorSparseIndices.getByteOffset(), 0);
        return AccessorDatas.create(
            componentType, bufferViewData, byteOffset, count, 1, null);
    }
    
    /**
     * Create the {@link AccessorData} for the given 
     * {@link AccessorSparseValues}
     * 
     * @param accessorSparseValues The {@link AccessorSparseValues}
     * @param componentType The component type of the {@link Accessor}
     * @param numComponentsPerElement The number of components per element
     * of the {@link AccessorModel#getElementType() accessor element type}
     * @param count The count from the {@link AccessorSparse} 
     * @return The {@link AccessorData}
     */
    private AccessorData createSparseValuesAccessorData(
        AccessorSparseValues accessorSparseValues, 
        int componentType, int numComponentsPerElement, int count) {
        Integer bufferViewIndex = accessorSparseValues.getBufferView();
        BufferViewModel bufferViewModel = bufferViewModels.get(bufferViewIndex);
        ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
        int byteOffset = Optionals.of(accessorSparseValues.getByteOffset(), 0);
        return AccessorDatas.create(
            componentType, bufferViewData, byteOffset, count, 
            numComponentsPerElement, null);
    }

    /**
     * Initialize the {@link BufferModel} instances
     */
    private void initBufferModels() {
        List<Buffer> buffers = Optionals.of(gltf.getBuffers());

        if (buffers.isEmpty() && binaryData != null) {
            logger.warning("Binary data was given, but no buffers");
            return;
        }

        for (int i = 0; i < buffers.size(); i++) {
            Buffer buffer = buffers.get(i);
            BufferModel bufferModel = bufferModels.get(i);
            bufferModel.setName(buffer.getName());
            if (i == 0 && binaryData != null) {
                bufferModel.setBufferData(binaryData);
            } else {
                String uri = buffer.getUri();
                if (IO.isDataUriString(uri)) {
                    byte data[] = IO.readDataUri(uri);
                    ByteBuffer bufferData = Buffers.create(data);
                    bufferModel.setBufferData(bufferData);
                } else {
                    ByteBuffer bufferData = gltfAsset.getReferenceData(uri);
                    bufferModel.setBufferData(bufferData);
                }
            }
        }
    }
    
    
    /**
     * Initialize the {@link BufferViewModel} instances
     */
    private void initBufferViewModels() {
        List<BufferView> bufferViews = Optionals.of(gltf.getBufferViews());
        for (int i = 0; i < bufferViews.size(); i++) {
            BufferView bufferView = bufferViews.get(i);
            
            BufferViewModel bufferViewModel = bufferViewModels.get(i);
            bufferViewModel.setName(bufferView.getName());
            
            int bufferIndex = bufferView.getBuffer();
            BufferModel bufferModel = bufferModels.get(bufferIndex);
            bufferViewModel.setBufferModel(bufferModel);
        }
    }
    

    /**
     * Initialize the {@link MeshModel} instances
     */
    private void initMeshModels() {
        List<Mesh> meshes = Optionals.of(gltf.getMeshes());
        for (int i = 0; i < meshes.size(); i++) {
            Mesh mesh = meshes.get(i);
            MeshModel meshModel = meshModels.get(i);
            meshModel.setName(mesh.getName());
            
            List<Primitive> primitives = Optionals.of(mesh.getPrimitives());
            for (Primitive primitive : primitives) {
                MeshPrimitiveModel meshPrimitiveModel = createMeshPrimitiveModel(primitive);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
        }
    }
    
    /**
     * Create a {@link MeshPrimitiveModel} for the given 
     * {@link Primitive}.<br>
     * <br>
     * Note: The resulting {@link MeshPrimitiveModel} will not have any
     * {@link MaterialModel} assigned. The material model may have to
     * instances. This is done in {@link #instantiateMaterialModels()}
     * 
     * @param primitive The {@link Primitive}
     * @return The {@link MeshPrimitiveModel}
     */
    private MeshPrimitiveModel createMeshPrimitiveModel(Primitive primitive) {
        Integer mode = Optionals.of(
            primitive.getMode(),
            primitive.defaultMode());
        MeshPrimitiveModel meshPrimitiveModel = new MeshPrimitiveModel(mode);
        
        Integer indicesIndex = primitive.getIndices();
        if (indicesIndex != null) {
            AccessorModel indices = accessorModels.get(indicesIndex);
            meshPrimitiveModel.setIndices(indices);
        }
        Map<String, Integer> attributes = Optionals.of(primitive.getAttributes());
        for (Entry<String, Integer> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            int attributeIndex = entry.getValue();
            AccessorModel attribute = accessorModels.get(attributeIndex);
            meshPrimitiveModel.putAttribute(attributeName, attribute);
        }
        
        List<Map<String, Integer>> morphTargets = Optionals.of(primitive.getTargets());
        for (Map<String, Integer> morphTarget : morphTargets) {
            Map<String, AccessorModel> morphTargetModel = new LinkedHashMap<String, AccessorModel>();
            for (Entry<String, Integer> entry : morphTarget.entrySet()) {
                String attribute = entry.getKey();
                Integer accessorIndex = entry.getValue();
                AccessorModel accessorModel = accessorModels.get(accessorIndex);
                morphTargetModel.put(attribute, accessorModel);
            }
            meshPrimitiveModel.addTarget(Collections.unmodifiableMap(morphTargetModel));
        }
        
        return meshPrimitiveModel;
    }

    /**
     * Initialize the {@link NodeModel} instances
     */
    private void initNodeModels() {
        List<Node> nodes = Optionals.of(gltf.getNodes());
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            
            NodeModel nodeModel = nodeModels.get(i);
            nodeModel.setName(node.getName());
            
            List<Integer> childIndices = Optionals.of(node.getChildren());
            for (Integer childIndex : childIndices) {
                NodeModel child = nodeModels.get(childIndex);
                nodeModel.addChild(child);
            }
            
            Integer meshIndex = node.getMesh();
            if (meshIndex != null) {
                MeshModel meshModel = meshModels.get(meshIndex);
                nodeModel.addMeshModel(meshModel);
            }
            
            float matrix[] = node.getMatrix();
            float translation[] = node.getTranslation();
            float rotation[] = node.getRotation();
            float scale[] = node.getScale();
            nodeModel.setMatrix(Optionals.clone(matrix));
            nodeModel.setTranslation(Optionals.clone(translation));
            nodeModel.setRotation(Optionals.clone(rotation));
            nodeModel.setScale(Optionals.clone(scale));
            
            List<Float> weights = node.getWeights();
            if (weights != null) {
                float weightsArray[] = new float[weights.size()];
                for (int j = 0; j < weights.size(); j++)
                {
                    weightsArray[j] = weights.get(j);
                }
                nodeModel.setWeights(weightsArray);
            }
        }
    }
    

    /**
     * Initialize the {@link SceneModel} instances
     */
    private void initSceneModels() {
        List<Scene> scenes = Optionals.of(gltf.getScenes());
        for (int i = 0; i < scenes.size(); i++) {
            Scene scene = scenes.get(i);

            SceneModel sceneModel = sceneModels.get(i);
            sceneModel.setName(scene.getName());
            
            List<Integer> nodeIndices = Optionals.of(scene.getNodes());
            for (Integer nodeIndex : nodeIndices)
            {
                NodeModel nodeModel = nodeModels.get(nodeIndex);
                sceneModel.addNode(nodeModel);
            }
        }
    }
    
    /**
     * Initialize the {@link TextureModel} instances
     */
    private void initTextureModels() {
        List<Texture> textures = Optionals.of(gltf.getTextures());
        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            TextureModel textureModel = textureModels.get(i);
            textureModel.setName(texture.getName());
            
            Integer imageIndex = texture.getSource();
            ImageModel imageModel = imageModels.get(imageIndex);
            textureModel.setImageModel(imageModel);
        }
    }
    
    /**
     * Initialize the {@link ImageModel} instances
     */
    private void initImageModels() {
        List<Image> images = Optionals.of(gltf.getImages());
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            ImageModel imageModel = imageModels.get(i);
            imageModel.setName(image.getName());
            
            Integer bufferViewIndex = image.getBufferView();
            if (bufferViewIndex != null) {
                BufferViewModel bufferViewModel = 
                    bufferViewModels.get(bufferViewIndex);
                imageModel.setBufferViewModel(bufferViewModel);
            } else {
                String uri = image.getUri();
                if (IO.isDataUriString(uri)) {
                    byte data[] = IO.readDataUri(uri);
                    ByteBuffer imageData = Buffers.create(data);
                    imageModel.setImageData(imageData);
                } else {
                    ByteBuffer imageData = gltfAsset.getReferenceData(uri);
                    imageModel.setImageData(imageData);
                }
            }
        }
    }
    

    /**
     * Create the {@link CameraModel} instances. This has to be be called
     * <b>after</b> the {@link #nodeModels} have been created: Each time
     * that a node refers to a camera, a new instance of this camera
     * has to be created.
     */
    private void instantiateCameraModels() {
        List<Node> nodes = Optionals.of(gltf.getNodes());
        List<Camera> cameras = Optionals.of(gltf.getCameras());
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            
            Integer cameraIndex = node.getCamera();
            if (cameraIndex != null) {
                Camera camera = cameras.get(cameraIndex);
                NodeModel nodeModel = nodeModels.get(i);
                
                Function<float[], float[]> viewMatrixComputer = result -> {
                    float localResult[] = Utils.validate(result, 16);
                    nodeModel.computeGlobalTransform(localResult);
                    MathUtils.invert4x4(localResult, localResult);
                    return localResult;
                };
                BiFunction<float[], Float, float[]> projectionMatrixComputer = (result, aspectRatio) ->
                {
                    float localResult[] = Utils.validate(result, 16);
                    Cameras.computeProjectionMatrix(camera, aspectRatio, localResult);
                    return localResult;
                };
                CameraModel cameraModel = new CameraModel(
                    viewMatrixComputer, projectionMatrixComputer);
                cameraModel.setName(camera.getName());
                
                cameraModel.setNodeModel(nodeModel);

                String nodeName = Optionals.of(node.getName(), "node" + i);
                String cameraName = 
                    Optionals.of(camera.getName(), "camera" + cameraIndex);
                String instanceName = nodeName + "." + cameraName;
                cameraModel.setInstanceName(instanceName);
                
                cameraModels.add(cameraModel);
            }
        }
    }
    
    /**
     * For each mesh that is instantiated in a node, call
     * {@link #instantiateMaterialModels(Mesh, MeshModel, int)} 
     */
    private void instantiateMaterialModels() {
        List<Node> nodes = Optionals.of(gltf.getNodes());
        List<Mesh> meshes = Optionals.of(gltf.getMeshes());
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Integer meshIndex = node.getMesh();
            if (meshIndex != null) {
                MeshModel meshModel = meshModels.get(meshIndex);
                
                int numJoints = 0;
                Mesh mesh = meshes.get(meshIndex);
                instantiateMaterialModels(mesh, meshModel, numJoints);
            }
        }
    }
    
    /**
     * Create the {@link MaterialModel} instances that are required for
     * rendering the {@link MeshPrimitiveModel} instances of the given 
     * {@link MeshModel}, based on the corresponding {@link Primitive}
     * and the given number of joints.
     *  
     * @param mesh The {@link Mesh}
     * @param meshModel The {@link MeshModel}
     * @param numJoints The number of joints
     */
    private void instantiateMaterialModels(Mesh mesh, MeshModel meshModel, int numJoints) {
        List<Primitive> primitives = mesh.getPrimitives();
        List<MeshPrimitiveModel> meshPrimitiveModels = meshModel.getMeshPrimitiveModels();
        
        for (int i = 0; i < primitives.size(); i++) {
            Primitive primitive = primitives.get(i);
            MeshPrimitiveModel meshPrimitiveModel = meshPrimitiveModels.get(i);

            Material material = null;
            Integer materialIndex = primitive.getMaterial();
            if (materialIndex == null) {
                material = Materials.createDefaultMaterial();
            } else {
                material = gltf.getMaterials().get(materialIndex);
            }
            MaterialModel materialModel = materialModelHandler.createMaterialModel(material, numJoints);
            materialModel.setName(material.getName());
            
            meshPrimitiveModel.setMaterialModel(materialModel);
            materialModels.add(materialModel);
        }
    }
    

    public List<AccessorModel> getAccessorModels()
    {
        return Collections.unmodifiableList(accessorModels);
    }

    public List<BufferModel> getBufferModels()
    {
        return Collections.unmodifiableList(bufferModels);
    }

    public List<BufferViewModel> getBufferViewModels()
    {
        return Collections.unmodifiableList(bufferViewModels);
    }

    public List<CameraModel> getCameraModels()
    {
        return Collections.unmodifiableList(cameraModels);
    }

    public List<ImageModel> getImageModels()
    {
        return Collections.unmodifiableList(imageModels);
    }

    public List<MaterialModel> getMaterialModels()
    {
        return Collections.unmodifiableList(materialModels);
    }

    public List<NodeModel> getNodeModels()
    {
        return Collections.unmodifiableList(nodeModels);
    }

    public List<SceneModel> getSceneModels()
    {
        return Collections.unmodifiableList(sceneModels);
    }

    public List<TextureModel> getTextureModels()
    {
        return Collections.unmodifiableList(textureModels);
    }

    public GlTF getGltf()
    {
        return gltf;
    }
    
}
