const targetDate = new Date(1744009200 * 1000);

setInterval(function() {
    const now = new Date().getTime();
    const distance = targetDate - now;

    const days = Math.max(0, Math.floor(distance / (1000 * 60 * 60 * 24)));
    const hours = Math.max(0, Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
    const minutes = Math.max(0, Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60)));
    const seconds = Math.max(0, Math.floor((distance % (1000 * 60)) / 1000));

    document.getElementById("days").innerText = formatTime(days);
    document.getElementById("hours").innerText = formatTime(hours);
    document.getElementById("minutes").innerText = formatTime(minutes);
    document.getElementById("seconds").innerText = formatTime(seconds);

    if (distance < 0) {
        clearInterval();
        window.location.reload();
    }
}, 1000);

function formatTime(time) {
    return time < 10 ? "0" + time : time;
}

window.onload = () => {

    setTimeout(() => {
        const phrase = "NOW I SEE THE LIGHT";
        const text = document.getElementById("text");

        let index = 0;
        function write() {
            if (index < phrase.length) {
                text.textContent += phrase[index];
                index++;
            } else {
                clearInterval(interval);
            }
        }
        const interval = setInterval(write, 50);
    }, 1500);
    document.getElementById("bruh").remove();
};