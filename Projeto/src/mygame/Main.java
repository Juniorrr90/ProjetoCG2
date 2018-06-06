package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * test
 * @author Jose Eduardo A Junior RA 111493
 */
public class Main extends SimpleApplication implements PhysicsCollisionListener, ActionListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = false;
        app.start();
    }
    
    private BulletAppState bulletAppState;
    private float mousex = 0, mousey = 15, tempo = 20;
    private Spatial JogadorMD;
    private static int altura, largura, pontuacao = 0, fimJogo = 1, vezesTocada = 1;
    private BitmapText info, menu;
    private int[] lamp = new int[9];
    private AudioNode acende_som, fimJogo_som, comeca_som;
    
    @Override
    public void simpleInitApp() {
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        inputManager.addMapping("Iniciar", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Apagar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Iniciar", "Apagar");
        
        inputManager.addMapping("MoveX", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MoveXI", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("MoveY", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("MoveYI", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addListener(analogListener, "MoveX");
        inputManager.addListener(analogListener, "MoveXI");
        inputManager.addListener(analogListener, "MoveY");
        inputManager.addListener(analogListener, "MoveYI");
                
        Scoreboard();
        Background();
        AmbientLights();
        Colors();
        Player();
        Sounds();
        Splash(); 
        
        for (int i = 0; i < 9; i++) {
            lamp[i] = 1;
        }

        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        cam.setLocation(new Vector3f(0,15,40));
        flyCam.setEnabled(false);
        
        largura = settings.getWidth();
        altura = settings.getHeight();
        mouseInput.setCursorVisible(false);
    }
    float tempoSplash = 7;
    boolean isFirst = true;
    @Override
    public void simpleUpdate(float tpf) {
        if(isFirst){
            guiNode.detachAllChildren();
            Scoreboard();
            isFirst = false;
        }
        if(tempoSplash <= 0){
            rootNode.detachChildNamed("paredeLogo");
            tempoSplash = 12;
        }
        else if(tempoSplash < 11 && tempoSplash > 0)
            tempoSplash-= tpf;
        
        if(tempoSplash > 2 && tempoSplash < 3)
        {
            tempoSplash = 2;
            //comeca_som.playInstance();
        }
        
        if(fimJogo == 0){
            NumberFormat formatarFloat;
            if(tempo < 10)
                formatarFloat = new DecimalFormat("#.##");
            else
                formatarFloat = new DecimalFormat("#");
            info.setText(" Pontos: " + pontuacao + "\n Tempo Restante: " + formatarFloat.format(tempo));
            tempo-= tpf;
        }
        
        if(tempo <= 0){
            fimJogo = 1;
            if(vezesTocada < 1){
                fimJogo_som.playInstance();
                vezesTocada++;
            }
        }
        
        if(fimJogo == 1 && tempoSplash == 12){
            menu.setText("I para iniciar um novo jogo");
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    private void AmbientLights() {       
        DirectionalLight l1 = new DirectionalLight();
        l1.setDirection(new Vector3f(1, -0.7f, 0));
        rootNode.addLight(l1);

        DirectionalLight l2 = new DirectionalLight();
        l2.setDirection(new Vector3f(-1,0,0));
        rootNode.addLight(l2);

        DirectionalLight l3 = new DirectionalLight();
        l3.setDirection(new Vector3f(0, 0, -1.0f));
        rootNode.addLight(l3);
        
        DirectionalLight l4 = new DirectionalLight();
        l4.setDirection(new Vector3f(0, 0, 1.0f));
        rootNode.addLight(l4);
        
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
    }
    
    private void Background() {
        Box meshTerreno = new Box(50,0.1f,50);
        Geometry terreno = new Geometry("Terreno", meshTerreno);

        Texture texturaTerreno = assetManager.loadTexture("Textures/parede_texture.png");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texturaTerreno);
        terreno.setMaterial(mat);

        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(terreno);
        RigidBodyControl corpoRigido = new RigidBodyControl(sceneShape, 0);                
        terreno.addControl(corpoRigido);

        bulletAppState.getPhysicsSpace().add(terreno);
        
        Texture texturaParede = assetManager.loadTexture("Textures/parede_texture.png");
        
        Box meshParedeEsq = new Box(2, 70, 50);
        Geometry paredeEsq = new Geometry("ParedeEsq", meshParedeEsq);
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setTexture("ColorMap", texturaParede);
        paredeEsq.setMaterial(mat2);
        paredeEsq.setLocalTranslation(-35, 0, 0);
        
        Box meshParedeDir = new Box(2, 70, 50);
        Geometry paredeDir = new Geometry("ParedeDir", meshParedeDir);
        mat2.setTexture("ColorMap", texturaParede);
        paredeDir.setMaterial(mat2);
        paredeDir.setLocalTranslation(35, 0, 0);
        
        Box meshParedeFundo = new Box(50, 70, 2);
        Geometry paredeFun = new Geometry("ParedeFun", meshParedeFundo);
        mat2.setTexture("ColorMap", texturaParede);
        paredeFun.setMaterial(mat2);
        paredeFun.setLocalTranslation(0, 0, -50);
        
        Box meshPainel = new Box(20, 30, 2);
        Geometry painel = new Geometry("Painel", meshPainel);
        Texture texturaPainel = assetManager.loadTexture("Textures/painel_texture.jpg");
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setTexture("ColorMap", texturaPainel);
        painel.setMaterial(mat3);
        painel.setLocalTranslation(0, 0, -30);
        
        RigidBodyControl corpoPainel = new RigidBodyControl(0f);
        painel.addControl(corpoPainel);
        corpoPainel.setPhysicsLocation(painel.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoPainel);
        corpoPainel.getPhysicsSpace().addCollisionListener(this);
        
        rootNode.attachChild(terreno);
        rootNode.attachChild(paredeEsq);
        rootNode.attachChild(paredeDir);
        rootNode.attachChild(paredeFun);
        rootNode.attachChild(painel);
    }

    public void collision(PhysicsCollisionEvent event) {
        //Cor1
        if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaSE")) {
                if(lamp[0] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[0] = 1; 
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaSE")) {
                if(lamp[0] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[0] = 1; 
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
        //Cor2
        if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaSM")) {
                if(lamp[1] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[1] = 1; 
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaSM")) {
                if(lamp[1] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[1] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor3
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaSD")) {
                if(lamp[2] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[2] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaSD")) {
                if(lamp[2] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[2] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor4
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaME")) {
                if(lamp[3] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[3] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaME")) {
                if(lamp[3] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[3] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor5
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaMM")) {
                if(lamp[4] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[4] = 1; 
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaMM")) {
                if(lamp[4] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[4] = 1;
                    acendeLuz();
                    
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor6
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaMD")) {
                if(lamp[5] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[5] = 1;
                    acendeLuz();
                    
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaMD")) {
                if(lamp[5] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[5] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor7
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaIE")) {
                if(lamp[6] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[6] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaIE")) {
                if(lamp[6] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[6] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor8
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaIM")) {
                if(lamp[7] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[7] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaIM")) {
                if(lamp[7] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[7] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }
		
	//Cor9
	if (event.getNodeA().getName().equals("tiro")) {
            if (event.getNodeB().getName().equals("lampadaID")) {
                if(lamp[8] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeB().setMaterial(mat);
                    lamp[8] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeA().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeA()); 
        }

        if (event.getNodeB().getName().equals("tiro")) {
            if (event.getNodeA().getName().equals("lampadaID")) {
                if(lamp[8] == 0){
                    pontuacao += 20;
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setColor("Color", ColorRGBA.Cyan);
                    event.getNodeA().setMaterial(mat);
                    lamp[8] = 1;
                    acendeLuz();
                }
                else if(!valida)
                {
                    pontuacao -= 10;
                    valida=true;
                }
            }
            bulletAppState.getPhysicsSpace().remove(event.getNodeB().getControl(RigidBodyControl.class));
            rootNode.detachChild(event.getNodeB()); 
        }	
    }
    boolean valida = false;
    int auxContador = 0;
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Iniciar") && fimJogo == 1) {
            fimJogo = 0;
            pontuacao = 0;
            tempo = 20; // Tempo de Jogo
            menu.setText(" ");
            vezesTocada = 0;
            apagaLuzes();
            acendeLuz();
       }
        
       if (name.equals("Apagar") && fimJogo == 0){
           if(auxContador%2 == 0){
                JogadorMD.setLocalTranslation(JogadorMD.getLocalTranslation().x, JogadorMD.getLocalTranslation().y, -25);

                Box meshTiro = new Box(0.1f,0.1f,4f);

                Geometry tiro = new Geometry("tiro", meshTiro);
                Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat1.setTexture("ColorMap", assetManager.loadTexture("Textures/transparente_texture.png"));
                mat1.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                tiro.setQueueBucket(RenderQueue.Bucket.Transparent);
                tiro.setMaterial(mat1);
                tiro.setLocalTranslation(JogadorMD.getLocalTranslation().x, JogadorMD.getLocalTranslation().y, JogadorMD.getLocalTranslation().z);
                RigidBodyControl corpoTiro = new RigidBodyControl(10f);
                tiro.addControl(corpoTiro);
                corpoTiro.setPhysicsLocation(tiro.getLocalTranslation());
                bulletAppState.getPhysicsSpace().add(corpoTiro);
                corpoTiro.getPhysicsSpace().addCollisionListener(this);
                //rootNode.attachChild(tiro);
                valida=false;
           }
           auxContador++;
       }
       else
       {
           valida=false;
       }
    }

    private void Colors() {
        Sphere meshLamp = new Sphere(10, 15, 3f);
        Geometry lampadaSE = new Geometry("lampadaSE", meshLamp);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Cyan);
        lampadaSE.setMaterial(mat1);
        lampadaSE.setLocalTranslation(-13, 25, -28);
        
        RigidBodyControl corpoRigidoSE = new RigidBodyControl(0f);
        lampadaSE.addControl(corpoRigidoSE);
        corpoRigidoSE.setPhysicsLocation(lampadaSE.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoSE);
        corpoRigidoSE.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaSM = new Geometry("lampadaSM", meshLamp);
        lampadaSM.setMaterial(mat1);
        lampadaSM.setLocalTranslation(0, 25, -28);
		
	RigidBodyControl corpoRigidoSM = new RigidBodyControl(0f);
        lampadaSM.addControl(corpoRigidoSM);
        corpoRigidoSM.setPhysicsLocation(lampadaSM.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoSM);
        corpoRigidoSM.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaSD = new Geometry("lampadaSD", meshLamp);
        lampadaSD.setMaterial(mat1);
        lampadaSD.setLocalTranslation(13, 25, -28);
		
	RigidBodyControl corpoRigidoSD = new RigidBodyControl(0f);
        lampadaSD.addControl(corpoRigidoSD);
        corpoRigidoSD.setPhysicsLocation(lampadaSD.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoSD);
        corpoRigidoSD.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaME = new Geometry("lampadaME", meshLamp);
        lampadaME.setMaterial(mat1);
        lampadaME.setLocalTranslation(-13, 15, -28);
		
	RigidBodyControl corpoRigidoME = new RigidBodyControl(0f);
        lampadaME.addControl(corpoRigidoME);
        corpoRigidoME.setPhysicsLocation(lampadaME.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoME);
        corpoRigidoME.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaMM = new Geometry("lampadaMM", meshLamp);
        lampadaMM.setMaterial(mat1);
        lampadaMM.setLocalTranslation(0, 15, -28);
		
	RigidBodyControl corpoRigidoMM = new RigidBodyControl(0f);
        lampadaMM.addControl(corpoRigidoMM);
        corpoRigidoMM.setPhysicsLocation(lampadaMM.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoMM);
        corpoRigidoMM.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaMD = new Geometry("lampadaMD", meshLamp);
        lampadaMD.setMaterial(mat1);
        lampadaMD.setLocalTranslation(13, 15, -28);
		
	RigidBodyControl corpoRigidoMD = new RigidBodyControl(0f);
        lampadaMD.addControl(corpoRigidoMD);
        corpoRigidoMD.setPhysicsLocation(lampadaMD.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoMD);
        corpoRigidoMD.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaIE = new Geometry("lampadaIE", meshLamp);
        lampadaIE.setMaterial(mat1);
        lampadaIE.setLocalTranslation(-13, 5, -28);
		
	RigidBodyControl corpoRigidoIE = new RigidBodyControl(0f);
        lampadaIE.addControl(corpoRigidoIE);
        corpoRigidoIE.setPhysicsLocation(lampadaIE.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoIE);
        corpoRigidoIE.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaIM = new Geometry("lampadaIM", meshLamp);
        lampadaIM.setMaterial(mat1);
        lampadaIM.setLocalTranslation(0, 5, -28);
		
	RigidBodyControl corpoRigidoIM = new RigidBodyControl(0f);
        lampadaIM.addControl(corpoRigidoIM);
        corpoRigidoIM.setPhysicsLocation(lampadaIM.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoIM);
        corpoRigidoIM.getPhysicsSpace().addCollisionListener(this);
        
        Geometry lampadaID = new Geometry("lampadaID", meshLamp);
        lampadaID.setMaterial(mat1);
        lampadaID.setLocalTranslation(13, 5, -28);
		
	RigidBodyControl corpoRigidoID = new RigidBodyControl(0f);
        lampadaID.addControl(corpoRigidoID);
        corpoRigidoID.setPhysicsLocation(lampadaID.getLocalTranslation());
        bulletAppState.getPhysicsSpace().add(corpoRigidoID);
        corpoRigidoID.getPhysicsSpace().addCollisionListener(this);
        
        rootNode.attachChild(lampadaSE);
        rootNode.attachChild(lampadaSM);
        rootNode.attachChild(lampadaSD);
        
        rootNode.attachChild(lampadaME);
        rootNode.attachChild(lampadaMM);
        rootNode.attachChild(lampadaMD);
        
        rootNode.attachChild(lampadaIE);
        rootNode.attachChild(lampadaIM);
        rootNode.attachChild(lampadaID);
    }
    
    private void Player() {
       Box box = new Box(Vector3f.ZERO, 5, 5, 0.1f);
       JogadorMD = new Geometry("JogadorMD", box);
      
       Material mat_tt = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
       mat_tt.setTexture("ColorMap", assetManager.loadTexture("Textures/mao_texture.png"));
       mat_tt.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
       JogadorMD.setQueueBucket(RenderQueue.Bucket.Transparent);
       JogadorMD.setMaterial(mat_tt);
       JogadorMD.setLocalTranslation(0, 15, -23);
       
       rootNode.attachChild(JogadorMD);
    }
    private AnalogListener analogListener = new AnalogListener()
    {
        public void onAnalog(String name, float value, float tpf) 
        {
            if(name.equals("MoveX") || name.equals("MoveXI") || name.equals("MoveY") || name.equals("MoveYI")){
                
                mousex = ((inputManager.getCursorPosition().x-(largura/2))/40);
                if(altura < 600)
                    mousey = ((inputManager.getCursorPosition().y-((altura/8)))/40);
                else
                   mousey = ((inputManager.getCursorPosition().y-((altura/4)))/40); 
                    
                JogadorMD.setLocalTranslation(mousex, mousey, -23);
                JogadorMD.updateGeometricState();
            }
        }
    };
    
    private void Scoreboard() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        info = new BitmapText(guiFont, false);
        info.setSize(guiFont.getCharSet().getRenderedSize());
        info.setColor(ColorRGBA.White);
        info.setLocalTranslation(0, settings.getHeight() - 20, 0);
        guiNode.attachChild(info);
        
        menu = new BitmapText(guiFont, false);
        menu.setSize(guiFont.getCharSet().getRenderedSize());
        menu.setColor(ColorRGBA.White);
        menu.setLocalTranslation((settings.getWidth()/2)*0.75f, settings.getHeight() - 20, 0);
        guiNode.attachChild(menu);
    }
    
    private void acendeLuz(){
        double luz;
        do{
            luz = Math.random();
        }while(luz*10 >= 9);
        
        luz*=10;
        lamp[(int)luz] = 0;
        
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Yellow);
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Blue);
        Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat4.setColor("Color", ColorRGBA.Green);
        Material mat5 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat5.setColor("Color", ColorRGBA.Pink);
        Material mat6 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat6.setColor("Color", ColorRGBA.White);
        Material mat7 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat7.setColor("Color", ColorRGBA.Orange);
        Material mat8 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat8.setColor("Color", ColorRGBA.Brown);
        Material mat9 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat9.setColor("Color", ColorRGBA.Gray);
        
        if((int)luz == 0)
            rootNode.getChild("lampadaSE").setMaterial(mat1);
        else if((int)luz == 1)
            rootNode.getChild("lampadaSM").setMaterial(mat2);
        else if((int)luz == 2)
            rootNode.getChild("lampadaSD").setMaterial(mat3);
        else if((int)luz == 3)
            rootNode.getChild("lampadaME").setMaterial(mat4);
        else if((int)luz == 4)
            rootNode.getChild("lampadaMM").setMaterial(mat5);
        else if((int)luz == 5)
            rootNode.getChild("lampadaMD").setMaterial(mat6);
        else if((int)luz == 6)
            rootNode.getChild("lampadaIE").setMaterial(mat7);
        else if((int)luz == 7)
            rootNode.getChild("lampadaIM").setMaterial(mat8);
        else if((int)luz == 8)
            rootNode.getChild("lampadaID").setMaterial(mat9);
        
        acende_som.playInstance();
    }
    
    private void apagaLuzes(){
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Cyan);
        
        rootNode.getChild("lampadaSE").setMaterial(mat1);
        rootNode.getChild("lampadaSM").setMaterial(mat1);
        rootNode.getChild("lampadaSD").setMaterial(mat1);
        rootNode.getChild("lampadaME").setMaterial(mat1);
        rootNode.getChild("lampadaMM").setMaterial(mat1);
        rootNode.getChild("lampadaMD").setMaterial(mat1);
        rootNode.getChild("lampadaIE").setMaterial(mat1);
        rootNode.getChild("lampadaIM").setMaterial(mat1);
        rootNode.getChild("lampadaID").setMaterial(mat1);
    }
    
    private void Sounds(){
        acende_som = new AudioNode(assetManager, "Sounds/acender_som.wav", false);
        acende_som.setLooping(false);
        acende_som.setVolume(2);
        rootNode.attachChild(acende_som);
        
        fimJogo_som = new AudioNode(assetManager, "Sounds/fimJogo_som.wav", false);
        fimJogo_som.setLooping(false);
        fimJogo_som.setVolume(2);
        rootNode.attachChild(fimJogo_som);
    }
    
    private void Splash(){
        menu.setText(" ");
        
        Box meshLogo = new Box(18, 15, 2);
        Geometry paredeLogo = new Geometry("paredeLogo", meshLogo);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/logo_texture.png"));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        paredeLogo.setQueueBucket(RenderQueue.Bucket.Transparent);
        paredeLogo.setMaterial(mat);
        paredeLogo.setLocalTranslation(0, 15, 5);
        
        rootNode.attachChild(paredeLogo);
    }
}