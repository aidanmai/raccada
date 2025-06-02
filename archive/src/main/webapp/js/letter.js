const message = `Hello. Welcome to the Nereus Archive.\n\nWhat you will find inside are truths that were\nnot meant to be found. Tread carefully - some\nknowledge does not forget those who seek it.\n\nGood luck.\n\nR.`;

window.onload = () => {
    setTimeout(() => {
        const lines = message.split("\n");
        const messageDiv = document.getElementById("message");

        lines.forEach(function(line, lineIndex) {
            if(line === "") {
                messageDiv.appendChild(document.createElement("br"));
            } else {
                const lineDiv = document.createElement("div");
                lineDiv.style.textAlign = "left";
                lineDiv.style.height = "16px";
                lineDiv.style.width = "100%";
                messageDiv.appendChild(lineDiv);
                function write() {
                    let index = 0;
                    const interval = setInterval(function() {
                        if (index < line.length) {
                            lineDiv.textContent += line[index];
                            index++;
                        } else {
                            clearInterval(interval);
                        }
                    }, 10);
                }
                setTimeout(write, 100 * lineIndex);
            }
        });

        for(let i = 0; i < 4; i++) {messageDiv.appendChild(document.createElement("br"));}
    }, 1500);
    document.getElementById("bruh").remove();
}

function back() {
    document.querySelectorAll('.back').forEach(element => {element.style.animation = 'fadeOutUp 1s backwards';});
    document.querySelectorAll('#message').forEach(element => {element.style.animation = 'fadeOutDown 1s forwards 0.1s';});
    setTimeout(function() {
        window.location.reload();
    }, 1000);
}