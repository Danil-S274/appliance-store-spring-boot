(function () {
    document.querySelectorAll(".alert").forEach((el) => {
        const isDanger = el.classList.contains("alert-danger");
        const isWarning = el.classList.contains("alert-warning");
        if (isDanger || isWarning) return;

        el.classList.add("fade-soft");
        setTimeout(() => {
            el.classList.add("hide");
            setTimeout(() => el.remove(), 400);
        }, 3500);
    });

    const firstInvalid =
        document.querySelector(".is-invalid") ||
        document.querySelector("[aria-invalid='true']");
    if (firstInvalid) {
        try {
            firstInvalid.focus({ preventScroll: true });
            firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
        } catch (_) {}
    }

    // Toast helper (optional)
    window.appToast = function (message, variant = "dark") {
        // variant: 'dark' | 'success' | 'danger'
        let container = document.getElementById("toastContainer");
        if (!container) {
            container = document.createElement("div");
            container.id = "toastContainer";
            container.style.position = "fixed";
            container.style.right = "16px";
            container.style.bottom = "16px";
            container.style.zIndex = "1080";
            container.style.display = "flex";
            container.style.flexDirection = "column";
            container.style.gap = "10px";
            document.body.appendChild(container);
        }

        const toast = document.createElement("div");
        toast.className = `toast align-items-center text-bg-${variant} border-0 show`;
        toast.role = "alert";
        toast.style.minWidth = "260px";
        toast.style.borderRadius = "16px";
        toast.style.boxShadow = "0 18px 50px rgba(0,0,0,.35)";
        toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" aria-label="Close"></button>
      </div>
    `;

        toast.querySelector("button").addEventListener("click", () => toast.remove());
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = "0";
            toast.style.transform = "translateY(6px)";
            toast.style.transition = "all .3s ease";
            setTimeout(() => toast.remove(), 320);
        }, 3200);
    };
})();

