/*
import * as THREE from './three.module.js';
import { GLTFLoader } from './GLTFLoader.js';

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, 800/600, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
renderer.setSize(800, 600);
document.getElementById('falcon3d').appendChild(renderer.domElement);

const light = new THREE.DirectionalLight(0xffffff, 1);
light.position.set(10, 20, 10);
scene.add(light);
scene.add(new THREE.AmbientLight(0x888888));

camera.position.set(0, 4, 48);

const loader = new GLTFLoader();
loader.load('./models/millennium_falcon.glb', function(gltf) {
    const model = gltf.scene;
    model.scale.set(2,2,2);
    scene.add(model);

    function animate() {
        requestAnimationFrame(animate);
        model.rotation.y += 0.005;
        renderer.render(scene, camera);
    }
    animate();
}, undefined, function(error) {
    console.error(error);
});*/

import * as THREE from './three.module.js';
import { GLTFLoader } from './GLTFLoader.js';

const container = document.getElementById('falcon3d');
const width = container.clientWidth;
const height = container.clientHeight;

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000);
const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
renderer.setSize(width, height);
container.appendChild(renderer.domElement);

const light = new THREE.DirectionalLight(0xffffff, 1);
light.position.set(10, 20, 10);
scene.add(light);
scene.add(new THREE.AmbientLight(0x888888));

const loader = new GLTFLoader();
loader.load('./models/millennium_falcon.glb', function(gltf) {
    const model = gltf.scene;
    model.scale.set(4, 4, 4);
    scene.add(model);

    // Centrar el modelo
    const box = new THREE.Box3().setFromObject(model);
    const center = box.getCenter(new THREE.Vector3());
    model.position.sub(center); // Centra el modelo en el origen

    // Ajustar la cámara para que el modelo se vea completo
    const size = box.getSize(new THREE.Vector3()).length();
    const distance = size / (2 * Math.tan(Math.PI * camera.fov / 360));
    camera.position.set(0, 0, distance * 1.2);
    camera.lookAt(0, 0, 0);

    function animate() {
        requestAnimationFrame(animate);
        model.rotation.y += 0.005;
        renderer.render(scene, camera);
    }
    animate();
}, undefined, function(error) {
    console.error(error);
});