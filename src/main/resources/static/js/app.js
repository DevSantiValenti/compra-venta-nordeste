document.addEventListener("DOMContentLoaded", () => {
  const expectedPageSize = () => window.matchMedia("(max-width: 759px)").matches ? 12 : 24;
  const syncPageSizeInputs = () => {
    const size = expectedPageSize();
    document.querySelectorAll("[data-page-size-input]").forEach((input) => {
      input.value = String(size);
    });
    return size;
  };

  const currentExpectedPageSize = syncPageSizeInputs();

  document.querySelectorAll("[data-responsive-pagination]").forEach((form) => {
    form.addEventListener("submit", () => {
      const pageSize = syncPageSizeInputs();
      const pageInput = form.querySelector('input[name="page"]');
      const pageSizeInput = form.querySelector("[data-page-size-input]");
      if (pageInput) pageInput.value = "0";
      if (pageSizeInput) pageSizeInput.value = String(pageSize);
    });
  });

  if (document.querySelector("[data-responsive-pagination]") && window.location.pathname === "/") {
    const url = new URL(window.location.href);
    const currentPageSize = url.searchParams.get("pageSize");
    if (currentPageSize !== String(currentExpectedPageSize) && (currentPageSize || currentExpectedPageSize === 12)) {
      url.searchParams.set("pageSize", String(currentExpectedPageSize));
      url.searchParams.set("page", "0");
      window.location.replace(url.toString());
      return;
    }
  }

  document.querySelectorAll("[data-filter-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const target = document.querySelector(button.getAttribute("data-filter-toggle"));
      if (target) target.classList.toggle("open");
    });
  });

  document.querySelectorAll("[data-auto-submit]").forEach((input) => {
    input.addEventListener("change", () => {
      input.form?.requestSubmit();
    });
  });

  document.querySelectorAll("[data-image-preview]").forEach((input) => {
    input.addEventListener("change", () => {
      const target = document.querySelector(input.dataset.imagePreview);
      if (!target) return;
      target.innerHTML = "";
      const files = Array.from(input.files || []).slice(0, 5);
      const allowedTypes = ["image/jpeg", "image/png"];

      files.forEach((file) => {
        const preview = document.createElement("div");
        preview.className = "preview-item";

        if (!allowedTypes.includes(file.type)) {
          preview.classList.add("invalid");
          preview.textContent = "JPG/PNG";
          target.appendChild(preview);
          return;
        }

        const image = document.createElement("img");
        image.className = "preview-thumb";
        image.alt = file.name;
        preview.appendChild(image);
        target.appendChild(preview);

        const reader = new FileReader();
        reader.addEventListener("load", () => {
          image.src = reader.result;
        });
        reader.readAsDataURL(file);
      });

      if (files.some((file) => !allowedTypes.includes(file.type))) {
        const message = document.createElement("p");
        message.className = "preview-error";
        message.textContent = "Solo se pueden subir imagenes JPG o PNG.";
        target.appendChild(message);
      }
    });
  });

  document.querySelectorAll("[data-avatar-upload]").forEach((input) => {
    input.addEventListener("change", () => {
      const file = input.files && input.files[0];
      if (!file || !["image/jpeg", "image/png"].includes(file.type)) return;
      window.setTimeout(() => input.form?.requestSubmit(), 120);
    });
  });

  document.querySelectorAll("[data-favorite-product]").forEach((button) => {
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    const slug = button.dataset.favoriteProduct;
    const authenticated = button.dataset.favoriteAuthenticated === "true";

    const applyState = (active) => {
      button.classList.toggle("active", active);
      button.setAttribute("aria-pressed", String(active));
      const label = button.querySelector(".detail-action-label");
      const icon = button.querySelector(".detail-action-icon");
      if (label) label.textContent = active ? "Guardado" : "Favorito";
      if (icon) icon.textContent = active ? "♥" : "♡";
    };

    applyState(button.getAttribute("aria-pressed") === "true");
    button.addEventListener("click", async () => {
      if (!authenticated) {
        window.location.href = "/login";
        return;
      }

      const active = button.getAttribute("aria-pressed") === "true";
      const headers = csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {};
      const response = await fetch(`/user/favorites/${encodeURIComponent(slug)}`, {
        method: active ? "DELETE" : "POST",
        headers
      });

      if (!response.ok) return;
      const body = await response.json();
      applyState(body.favorite === true);
    });
  });

  document.querySelectorAll("[data-share-url]").forEach((button) => {
    button.addEventListener("click", async () => {
      const url = new URL(button.dataset.shareUrl, window.location.origin).toString();
      const title = button.dataset.shareTitle || document.title;

      try {
        if (navigator.share) {
          await navigator.share({ title, url });
        } else if (navigator.clipboard) {
          await navigator.clipboard.writeText(url);
          button.classList.add("copied");
          const label = button.querySelector(".detail-action-label");
          if (label) label.textContent = "Copiado";
          window.setTimeout(() => {
            button.classList.remove("copied");
            if (label) label.textContent = "Compartir";
          }, 1800);
        }
      } catch (error) {
        button.classList.remove("copied");
      }
    });
  });

  const modal = document.querySelector("#confirmModal");
  const title = document.querySelector("#confirmTitle");
  const body = document.querySelector("#confirmBody");
  const icon = document.querySelector("#confirmIcon");
  const submit = document.querySelector("#confirmSubmit");
  let pendingForm = null;

  const closeModal = () => {
    if (!modal) return;
    modal.classList.remove("open");
    modal.setAttribute("aria-hidden", "true");
    pendingForm = null;
  };

  document.querySelectorAll(".js-confirm-action").forEach((button) => {
    button.addEventListener("click", (event) => {
      if (!modal) return;
      event.preventDefault();
      pendingForm = button.closest("form");
      title.textContent = button.dataset.confirmTitle || "Confirmar acción";
      body.textContent = button.dataset.confirmBody || "Esta acción modificará la publicación.";
      icon.textContent = button.dataset.confirmIcon || "!";
      submit.textContent = button.dataset.confirmLabel || "Confirmar";
      submit.className = button.dataset.confirmDanger === "true" ? "btn danger" : "btn";
      modal.classList.add("open");
      modal.setAttribute("aria-hidden", "false");
    });
  });

  document.querySelectorAll("[data-confirm-cancel]").forEach((button) => {
    button.addEventListener("click", closeModal);
  });

  if (submit) {
    submit.addEventListener("click", () => {
      if (pendingForm) pendingForm.submit();
    });
  }

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeModal();
  });
});
