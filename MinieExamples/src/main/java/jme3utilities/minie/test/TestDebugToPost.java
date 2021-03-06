/*
 Copyright (c) 2020, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.minie.test;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;

/**
 * A SimpleApplication to test debug visualization in a post ViewPort.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestDebugToPost extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestDebugToPost.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TestDebugToPost.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * space for physics simulation
     */
    private PhysicsSpace physicsSpace;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestDebugToPost application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        /*
         * Mute the chatty loggers in certain packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        Application application = new TestDebugToPost();
        /*
         * Customize the window's title bar.
         */
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);

        settings.setAudioRenderer(null);
        application.setSettings(settings);
        application.start();
    }
    // *************************************************************************
    // SimpleApplication methods

    /**
     * Initialize the TestDebugToPost application.
     */
    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10f);
        flyCam.setZoomSpeed(10f);
        /*
         * Set up Bullet physics (with debug enabled).
         */
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);
        stateManager.attach(bulletAppState);
        /*
         * Direct debug visuals to a post ViewPort that clears the depth buffer.
         * This prevents z-fighting between the box and its debug visuals,
         * but allows debug visuals to overdraw the GUI.
         */
        ViewPort overlay = renderManager.createPostView("Overlay", cam);
        overlay.setClearFlags(false, true, false);
        bulletAppState.setDebugViewPorts(overlay);

        physicsSpace = bulletAppState.getPhysicsSpace();
        addBox();
    }

    /**
     * Add a large, static, gray box to the scene.
     */
    private void addBox() {
        float halfExtent = 1f; // mesh units
        Mesh mesh = new Box(halfExtent, halfExtent, halfExtent);
        Geometry geometry = new Geometry("box", mesh);
        rootNode.attachChild(geometry);

        Material boxMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Gray);
        geometry.setMaterial(boxMaterial);

        BoxCollisionShape shape = new BoxCollisionShape(halfExtent);
        RigidBodyControl boxBody
                = new RigidBodyControl(shape, PhysicsBody.massForStatic);
        geometry.addControl(boxBody);
        boxBody.setPhysicsSpace(physicsSpace);
    }
}
