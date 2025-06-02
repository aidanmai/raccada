let writing = false;

function bar(s1, s2) {
    result = "";
    for (let i = 0; i < s1.length; i++) {
        result += String.fromCharCode(s1.charCodeAt(i) ^ s2.charCodeAt(i % s2.length));
    }
    return result;
}

async function foo(str) {
    const encoder = new TextEncoder();
    const data = encoder.encode(str);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('');
    return hashHex;
}

let firstAttemptTime = null;
let attemptCount = 0;
let waiting = false;
function checkPassword() {
    if (writing) return;
    const passwordInput = document.getElementById("password");
    if (passwordInput.value === "") {
        passwordInput.style.opacity = 100;
        passwordInput.style.transform = "none";
        passwordInput.style.animation = "none";
        passwordInput.classList.add("shake");

        setTimeout(() => {
            passwordInput.classList.remove("shake");
            void passwordInput.offsetWidth;
        }, 300);
        return;
    }

    if(firstAttemptTime === null) firstAttemptTime = Math.floor(Date.now() / 1000)
    let currentTime = Math.floor(Date.now() / 1000);
    if(currentTime - firstAttemptTime > 60) {
        firstAttemptTime = currentTime;
        attemptCount = 0;
        waiting = false;
        clearText(25, "error");
    }
    if(attemptCount >= 6) {
        if(!waiting) writeText("Please wait a bit before trying again!", 25, "error");
        waiting = true;
        return;
    }
    attemptCount++;
    
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/5f4dcc3b5aa765d61d8327deb882cf9a");
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    xhr.onload = async function() {
        if (xhr.status >= 200 && xhr.status < 300) {
            var response = JSON.parse(xhr.responseText);
            waiting = false;
            clearText(25, "error");
            if(response["success"]) {
                const url = response["url"];

                await fade();

                setTimeout(() => {
                    window.location.href = url;
                }, 1000);
            } else {
                passwordInput.value = "";
                passwordInput.style.opacity = 100;
                passwordInput.style.transform = "none";
                passwordInput.style.animation = "none";
                passwordInput.classList.add("shake");

                setTimeout(() => {
                    passwordInput.classList.remove("shake");
                    void passwordInput.offsetWidth;
                }, 300);
            }
            if ("response" in response) {
                responseText = response["response"];
                await writeText(responseText, 25, "text");
            }
        } else if(xhr.status === 429) {
            if(!waiting) writeText("Please wait a bit before trying again!", 25, "error");
            waiting = true;
            attemptCount = 6;
        } else {
            writeText("An unknown error occurred. Try refreshing the page", 25, "error");
        }
    };


    (async () => {
        let key = await foo(bar("67160f88bf3863f134d530dd3f03d70ec19a8cb737a2b92793232a828b8ea2bc", passwordInput.value.toUpperCase()));
        var payload = {
            "password": passwordInput.value,
            "key": key
        };
        xhr.send(JSON.stringify(payload));
    })();
}

function handleKeyPress(event) {
    if (event.key === " ") {
        event.preventDefault();
    } else if (event.key === "Enter") {
        checkPassword();
    }
}

async function writeText(phrase, delay, id) {
    writing = true;
    text = document.getElementById(id);
    if(text.textContent.length > 0) {
        await clearText(25, id);
    }

    return new Promise((resolve) => {
        let index = 0;
        function write() {
            if (index < phrase.length) {
                text.textContent += phrase[index];
                index++;
            } else {
                clearInterval(interval);
                resolve();
                writing = false;
            }
        }
        const interval = setInterval(write, delay);
    });
}

function clearText(delay, id) {
    return new Promise((resolve) => {
        const text = document.getElementById(id);
        let textLength = text.textContent.length;

        function deleteCharacter() {
            if (textLength > 0) {
                text.textContent = text.textContent.slice(0, -1);
                textLength--;
                setTimeout(deleteCharacter, delay);
            } else {
                resolve();
            }
        }

        deleteCharacter();
    });
}

async function fade() {
    const passwordInput = document.getElementById("password");
    document.getElementById("logo").style.animation = "fadeOutScale 1s";
    passwordInput.style = "";
    passwordInput.style.animation = "fadeOutDown 1s";
    passwordInput.disabled = true;
    document.getElementById("arrow").style.animation = "fadeOutDown 1s";

    clearText(25, "error")
    await clearText(25, "text");
}

async function letter() {
    if(writing) return false;
    await fade();
    setTimeout(() => {
        window.location.href = "/f5b75010ea8a54b96f4fe7dafac65c18";
    }, 1000);
}

window.onload = () => {
    document.getElementById("password").addEventListener("keydown", handleKeyPress);
    document.getElementById("arrow").addEventListener("click", checkPassword);
    document.getElementById("logo").addEventListener("click", letter);

    writing = true;
    setTimeout(async () => {
        await writeText("NOW I SEE THE LIGHT", 50, "text");
    }, 1300);
    document.getElementById("bruh").remove();
};