const message = "Hello. You have come a very long way to get here. It is with \npleasure that I welcome you to the Nereus Archive.\n\nYou have proven yourself. You have conquered my toughest \nchallenges, following the most imperceptible of trails, and now \nyou stand at the threshold of truth. I have watched your progress \nin silence, hoping you would make it here. Now, at last, we can \nspeak freely.\n\nFirst, I owe you an apology. I have kept you in the dark - \ndeliberately, necessarily. There are powerful forces at work \nhere, who would do anything to see me silenced, to erase what I \nhave uncovered. Until now, I could not risk revealing my purpose. \nBut you have gained both my trust and respect. You are ready.\n\nLast year, I stumbled upon a trail of puzzles - not too \ndissimilar from the ones I've created for you. A series of \ncryptographic challenges, scattered across campus, luring only \nthe most persistent minds. As far as I'm aware, I was the only \none who was able to follow them to their end. And at the final \nintersection, I learned the truth: the puzzles were a test, a \nrecruitment drive by an organization that does not exist. No \nname. No records. No faces. Only a purpose - to expose the Nereus \nProject.\n\nThe Nereus Project is real. Buried deep beneath UCSD's campus, \nhidden behind layers upon layers of coverups and secrecy, is a \nsinister project woven into the very fabric of this university's \ncreation. I spent the last year chasing leads, securing highly \nclassified files, inching closer and closer to the truth. And I \nwas not alone - our organization worked together in secrecy, \nnever even knowing each other's names and faces, piecing together \na puzzle that should never have existed.\n\nWe were very, very careful. But they still found us.\n\nOne by one, my allies disappeared. Wiped clean. Some fled. Some \nwent silent. As far as I know, I am the last one left. I have no \nillusions about my fate. My time is running out. Before they find \nme, I must pass on what I have learned. That is why I created \nthis. That is why you are here.\n\nThis archive contains everything. Every classified document, \nevery intercepted transmission, every fragment of the Nereus \nProject. It is all I have left to give. And now, it belongs to \nyou. It will become public at midnight on the night of April\n20 at:\n\ntvis7opnsmtweoj6d74xfvugs4wimmiqlnvcck4sfzhucpx24ephffyd.onion\n\nBy the time you are reading this, it is likely that I am already \ngone. If you choose to step away, I will not blame you. But if \nyou choose to continue - if you choose to carry this burden \nforward - you must be prepared for the consequences. But know \nthat you must not carry it alone. Inside this website I have \nhidden a logbook of everyone who has come this far. Use it. Find \nthe others. Connect. From here, it only gets more dangerous - and \nI won't be here to guide you any longer.\n\nThe truth is here, waiting in the dark. It only needs someone \nbrave enough to bring it into the light.\n\nThe choice is yours.\n\nR.";

window.onload = () => {
    setTimeout(() => {
        const lines = message.split("\n");
        const messageDiv = document.getElementById("message");

        for(let i = 0; i < 4; i++) {messageDiv.appendChild(document.createElement("br"));}

        lines.forEach(function(line, lineIndex) {
            if(line === "") {
                messageDiv.appendChild(document.createElement("br"));
            } else {
                const lineDiv = document.createElement("div");
                lineDiv.style.textAlign = "left";
                lineDiv.style.minHeight = "18.5px";
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

        for(let i = 0; i < 2; i++) {messageDiv.appendChild(document.createElement("br"));}
    }, 1500);
    document.getElementById("bruh").remove();
}

function back() {
    document.querySelectorAll('#message').forEach(element => {element.style.animation = 'fadeOutUp 1s forwards 0.4s';});
    document.querySelectorAll('#links').forEach(element => {element.style.animation = 'fadeOutDown 1s forwards 0.4s';});
    setTimeout(function() {
        window.location.reload();
    }, 1300);
}

function logbook() {
    document.querySelectorAll('#message').forEach(element => {element.style.animation = 'fadeOutUp 1s forwards 0.4s';});
    document.querySelectorAll('#links').forEach(element => {element.style.animation = 'fadeOutDown 1s forwards 0.4s';});
    setTimeout(function() {
        window.location = "/10272e4971534cc9e54abe4c550b6336";
    }, 1300);
}