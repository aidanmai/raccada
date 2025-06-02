function validate() {
    const username = document.getElementById("username").value.replace(/ +/g, ' ').trim();
    const message = document.getElementById("message").value.replace(/ +/g, ' ').trim();
    const usernameAllowedChars = /^[a-z0-9 \-_]*$/i;
    const messageAllowedChars = /^[a-z0-9 !?.,;:'"(){}\[\]<>\-_\\\/+=@#$%^&\*]*$/i;
    const newlines = /[\r\n]+/g;

    const error = document.getElementById("error");
    if(!usernameAllowedChars.test(username) || newlines.test(username)) {
        error.innerText = "Username contains invalid characters";
        return false;
    } 
    if(!messageAllowedChars.test(message) || newlines.test(message)) {
        error.innerText = "Message contains invalid characters";
        return false;
    } 
    if(username.length < 3) {
        error.innerText = "Username must be at least 3 characters";
        return false;
    }
    if(username.length > 20) {
        error.innerText = "Username is too long";
        return false;
    }
    if(message.length > 80) {
        error.innerText = "Message is too long";
        return false;
    }

    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/10272e4971534cc9e54abe4c550b6336");
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    xhr.onload = function() {
        if (xhr.status >= 200 && xhr.status < 300) {
            var response = JSON.parse(xhr.responseText);
            if(response["success"]) {
                if(response["valid"]) {
                    document.getElementById("form").submit();
                    return true;
                } else {
                    error.innerText = "\"" + username +  "\" is already taken";
                    return false;
                }
            } else {
                error.innerText = "An unknown error occurred";
                return false;
            }
        } else {
            error.innerText = "An internal error occurred";
            return false;
        }
    };
    xhr.send(JSON.stringify({"type": "checkUsername", "username": username}));
}

function restrictUsernameInput(event) {
    event.target.value = event.target.value.replace(/[^a-z0-9 \-_]/gi, '');
}

function restrictMessageInput(event) {
    event.target.value = event.target.value.replace(/[^a-z0-9 !?.,;:'"(){}\[\]<>\-_\\\/+=@#$%^&\*]/gi, '');
}

function filterUsernameLength(event) {
    if(event.target.value.length >= 20 && event.key.length === 1) event.preventDefault();
}

function filterMessageLength(event) {
    if(event.target.value.length >= 80 && event.key.length === 1) event.preventDefault();
}

let shown = false;
function showForm() {
    const form = document.getElementById("form");
    if(shown) {
        form.offsetHeight;
        form.style.opacity = 0;
        form.style.transform = "translateY(-20px)";
        setTimeout(function () {
            form.style.display = "none";
        }, 200);
    } else {
        form.style.display = "block";
        form.offsetHeight;
        form.style.opacity = 1;
        form.style.transform = "translateY(0px)";
    }
    shown = !shown;
}

function back() {
    document.querySelectorAll('.back').forEach(element => {element.style.animation = 'fadeOutUp 1s backwards';});
    document.querySelectorAll('.welcome').forEach(element => {element.style.animation = 'fadeOutUp 1s backwards 0.1s';});
    document.querySelectorAll('.status1').forEach(element => {element.style.animation = 'fadeOutUp 1s backwards 0.2s';});
    document.querySelectorAll('.status2').forEach(element => {element.style.animation = 'fadeOutUp 1s backwards 0.3s';});
    document.querySelectorAll('#form').forEach(element => {element.style.animation = 'fadeOutUp 1s forwards 0.4s';});
    document.querySelectorAll('#table').forEach(element => {element.style.animation = 'fadeOutDown 1s forwards 0.4s';});
    setTimeout(function() {
        window.location.reload();
    }, 1300);
}

window.onload = () => {
    const usernameBox = document.getElementById("username");
    const messageBox = document.getElementById("message");
    if(usernameBox !== null) {
        username.addEventListener("input", restrictUsernameInput);
        username.addEventListener("keydown", filterUsernameLength);
    }
    if(messageBox !== null) {
        message.addEventListener("input", restrictMessageInput);
        message.addEventListener("keydown", filterMessageLength);
    }

    const rows = document.querySelectorAll('#table .rowHidden');
    let delay = 100;
    
    function write(row, index) {
        const text = row.firstChild.innerText;
        row.firstChild.innerText = '';
        row.classList.remove("rowHidden");
        row.classList.add("row");
        let i = 0;
        
        setTimeout(function() {
            const interval = setInterval(function() {
                let char = text.charAt(i);
                if(char === ' ') {
                    row.firstChild.innerText += "\u00A0";
                } else {
                    row.firstChild.innerText += text.charAt(i);;
                }
                i++;
                
                if (i === text.length) {
                    clearInterval(interval);
                }
            }, 5);
        }, 500 + (delay * index));
    }
    
    rows.forEach((row, index) => {
        write(row, index);
    });
    document.getElementById("bruh").remove();
};