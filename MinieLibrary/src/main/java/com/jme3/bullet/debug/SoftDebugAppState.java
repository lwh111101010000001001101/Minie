/*
 * Copyright (c) 2009-2015 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.debug;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSoftSpace;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyAsset;

/**
 * An AppState to manage debug visualization of a PhysicsSoftSpace.
 *
 * @author Stephen Gold sgold@sonic.net
 *
 * Based on BulletSoftBodyDebugAppState by dokthar.
 */
public class SoftDebugAppState extends BulletDebugAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(SoftDebugAppState.class.getName());
    // *************************************************************************
    // fields

    /**
     * limit which clusters are visualized, or null to visualize no clusters
     */
    private BulletDebugAppState.DebugAppStateFilter clusterFilter;
    /**
     * map soft bodies to visualization nodes
     */
    private HashMap<PhysicsSoftBody, Node> softBodies = new HashMap<>(64);
    /**
     * material for visualizing all soft-body anchors
     */
    private Material anchorMaterial;
    /**
     * material for visualizing all soft-body clusters
     */
    private Material clusterMaterial;
    /**
     * material for visualizing all soft-body links
     */
    private Material linkMaterial;
    /**
     * materials for soft-body faces
     */
    final private Material[] faceMaterials = new Material[3];
    // *************************************************************************
    // constructors

    /**
     * Instantiate an app state to visualize the specified space using the
     * specified view ports. This constructor should be invoked only by
     * BulletSoftBodyAppState.
     *
     * @param space the PhysicsSoftSpace to visualize (not null, alias created)
     * @param viewPorts the view ports in which to render (not null, unaffected)
     * @param filter the filter to limit which objects are visualized, or null
     * to visualize all objects (may be null, alias created)
     * @param initListener the init listener, or null if none (may be null,
     * alias created)
     */
    public SoftDebugAppState(PhysicsSoftSpace space,
            ViewPort[] viewPorts,
            BulletDebugAppState.DebugAppStateFilter filter,
            DebugInitListener initListener) {
        super(space, viewPorts, filter, initListener);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the Material for visualizing soft-body anchors.
     *
     * @return the pre-existing instance (not null)
     */
    Material getAnchorMaterial() {
        assert anchorMaterial != null;
        return anchorMaterial;
    }

    /**
     * Access the filter that determines which clusters are visualized.
     *
     * @return the filter, or null if none
     */
    BulletDebugAppState.DebugAppStateFilter getClusterFilter() {
        return clusterFilter;
    }

    /**
     * Access the Material for visualizing soft-body clusters.
     *
     * @return the pre-existing instance (not null)
     */
    Material getClusterMaterial() {
        assert clusterMaterial != null;
        return clusterMaterial;
    }

    /**
     * Access a Material for visualizing soft-body faces.
     *
     * @param numSides 0&rarr;invisible, 1&rarr;single-sided material,
     * 2&rarr;double-sided Material
     * @return the pre-existing Material (not null)
     */
    Material getFaceMaterial(int numSides) {
        Material result = faceMaterials[numSides];
        assert result != null;
        return result;
    }

    /**
     * Access the Material for visualizing soft-body links.
     *
     * @return the pre-existing instance (not null)
     */
    Material getLinkMaterial() {
        assert linkMaterial != null;
        return linkMaterial;
    }

    /**
     * Alter which soft-body clusters are visualized.
     *
     * @param filter the desired filter, or null to visualize no clusters
     */
    public void setClusterFilter(
            BulletDebugAppState.DebugAppStateFilter filter) {
        clusterFilter = filter;
    }
    // *************************************************************************
    // BulletDebugAppState methods

    /**
     * Initialize the materials.
     *
     * @param am the application's AssetManager (not null)
     */
    @Override
    protected void setupMaterials(AssetManager am) {
        assert am != null;
        super.setupMaterials(am);

        anchorMaterial = MyAsset.createWireframeMaterial(am, ColorRGBA.Green);
        anchorMaterial.setName("anchorMaterial");

        String matDefPath = "MatDefs/wireframe/multicolor2.j3md";
        clusterMaterial = new Material(am, matDefPath);
        ColorRGBA clusterColor = new ColorRGBA(1f, 0f, 0f, 1f); // red
        clusterMaterial.setColor("Color", clusterColor); // creates an alias
        float shapeSize = 10f;
        clusterMaterial.setFloat("PointSize", shapeSize);
        clusterMaterial.setName("clusterMaterial");
        String shapePath = "Textures/shapes/lozenge.png";
        boolean mipmaps = false;
        Texture shapeTexture = MyAsset.loadTexture(am, shapePath, mipmaps);
        clusterMaterial.setTexture("PointShape", shapeTexture);
        RenderState renderState = clusterMaterial.getAdditionalRenderState();
        renderState.setBlendMode(RenderState.BlendMode.Alpha);
        renderState.setDepthTest(false);

        faceMaterials[0] = MyAsset.createInvisibleMaterial(am);
        faceMaterials[1] = MyAsset.createWireframeMaterial(am, ColorRGBA.Red);
        faceMaterials[1].setName("debug red ss");
        faceMaterials[2] = MyAsset.createWireframeMaterial(am, ColorRGBA.Red);
        faceMaterials[2].setName("debug red ds");
        renderState = faceMaterials[2].getAdditionalRenderState();
        renderState.setFaceCullMode(RenderState.FaceCullMode.Off);

        linkMaterial = MyAsset.createWireframeMaterial(am, ColorRGBA.Orange);
        linkMaterial.setName("linkMaterial");
    }

    /**
     * Update this state prior to rendering. Should be invoked only by a
     * subclass or by the AppStateManager. Invoked once per frame, provided the
     * state is attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        updateSoftBodies();
        super.update(tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Synchronize the soft-body debug controls with the soft bodies in the
     * PhysicsSpoftSpace.
     */
    private void updateSoftBodies() {
        HashMap<PhysicsSoftBody, Node> oldMap = softBodies;
        //create new map
        softBodies = new HashMap<>(oldMap.size());
        PhysicsSoftSpace pSpace = (PhysicsSoftSpace) getPhysicsSpace();
        Collection<PhysicsSoftBody> list = pSpace.getSoftBodyList();
        for (PhysicsSoftBody softBody : list) {
            if (filter == null || filter.displayObject(softBody)) {
                Node node = oldMap.remove(softBody);
                if (node == null) {
                    node = new Node(softBody.toString());
                    attachChild(node);

                    logger.log(Level.FINE, "Create new SoftBodyDebugControl");
                    Control control = new SoftBodyDebugControl(this, softBody);
                    node.addControl(control);
                }
                softBodies.put(softBody, node);
                updateAxes(node);
            }
        }
        // Detach any leftover nodes.
        for (Node node : oldMap.values()) {
            node.removeFromParent();
        }
    }
}
