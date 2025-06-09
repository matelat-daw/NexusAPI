// Genera estrellas aleatorias en el canvas
const canvas = document.querySelector('.stars-canvas');
const ctx = canvas.getContext('2d');
let stars = [];

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    generateStars();
    drawStars();
}

function generateStars() {
    stars = [];
    for (let i = 0; i < 80; i++) {
        stars.push({
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            r: Math.random() * 2 + 1,
            opacity: Math.random() * 0.7 + 0.3
        });
    }
}

function drawStars() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    for (const star of stars) {
        ctx.globalAlpha = star.opacity;
        ctx.beginPath();
        ctx.arc(star.x, star.y, star.r, 0, 2 * Math.PI);
        ctx.fillStyle = '#fff';
        ctx.fill();
    }
    ctx.globalAlpha = 1.0;
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();