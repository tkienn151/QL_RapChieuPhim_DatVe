document.addEventListener('DOMContentLoaded', function () {

    var duongDan = window.location.pathname;
    document.querySelectorAll('.navbar [data-menu]').forEach(function (a) {
        var goc = a.getAttribute('data-menu');
        var khop = (goc === '/') ? (duongDan === '/') : (duongDan === goc || duongDan.indexOf(goc + '/') === 0);
        if (khop) a.classList.add('active');
    });

    document.querySelectorAll('.alert-success').forEach(function (el) {
        setTimeout(function () {
            if (window.bootstrap) { bootstrap.Alert.getOrCreateInstance(el).close(); }
            else { el.remove(); }
        }, 5000);
    });

    document.querySelectorAll('form[data-xac-nhan], form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            var msg = form.getAttribute('data-xac-nhan') || form.getAttribute('data-confirm');
            if (msg && !window.confirm(msg)) e.preventDefault();
        });
    });
    document.querySelectorAll('button[data-xac-nhan], a[data-xac-nhan], button[data-confirm], a[data-confirm]').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            var msg = btn.getAttribute('data-xac-nhan') || btn.getAttribute('data-confirm');
            if (msg && !window.confirm(msg)) e.preventDefault();
        });
    });

    document.querySelectorAll('input[data-trung-voi]').forEach(function (nhapLai) {
        var goc = document.getElementById(nhapLai.getAttribute('data-trung-voi'));
        if (!goc) return;
        function kiemTra() {
            nhapLai.setCustomValidity(nhapLai.value !== goc.value ? 'Mật khẩu nhập lại không khớp.' : '');
        }
        nhapLai.addEventListener('input', kiemTra);
        goc.addEventListener('input', kiemTra);
    });

    document.querySelectorAll('[data-hien-mat-khau]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.getAttribute('data-hien-mat-khau'));
            if (!input) return;
            var dangAn = input.type === 'password';
            input.type = dangAn ? 'text' : 'password';
            btn.setAttribute('aria-label', dangAn ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
            var icon = btn.querySelector('i');
            if (icon) {
                icon.className = dangAn ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    });

    var dirtyState = new WeakMap();
    var hoTroUserInvalid = typeof CSS !== 'undefined' && CSS.supports && CSS.supports('selector(:user-invalid)');

    function dongBoTrangThaiHopLe(el) {
        if (!el || typeof el.checkValidity !== 'function') return;
        if (hoTroUserInvalid) {
            el.setAttribute('aria-invalid', el.matches(':user-invalid') ? 'true' : 'false');
        } else {
            var hopLe = el.checkValidity();
            el.classList.toggle('user-invalid-fallback', !hopLe);
            el.classList.toggle('user-valid-fallback', hopLe);
            if (!hopLe) el.setAttribute('aria-invalid', 'true');
            else el.removeAttribute('aria-invalid');
        }
    }

    document.addEventListener('blur', function (e) {
        var el = e.target;
        if (!el || !el.classList || !el.classList.contains('form-control')) return;
        var st = dirtyState.get(el) || { hasInteracted: false, hasBlurred: false };
        st.hasBlurred = true;
        dirtyState.set(el, st);
        if (st.hasInteracted || hoTroUserInvalid) dongBoTrangThaiHopLe(el);
    }, true);

    document.addEventListener('input', function (e) {
        var el = e.target;
        if (!el || !el.classList || !el.classList.contains('form-control')) return;
        var st = dirtyState.get(el) || { hasInteracted: false, hasBlurred: false };
        st.hasInteracted = true;
        dirtyState.set(el, st);
        if (st.hasBlurred || el.hasAttribute('aria-invalid')) dongBoTrangThaiHopLe(el);
    });
});

document.addEventListener('error', function (e) {
    var el = e.target;
    if (el && el.tagName === 'IMG' && el.classList.contains('poster') && !el.dataset.daThayAnh) {
        el.dataset.daThayAnh = '1';
        el.src = '/images/no-poster.svg';
    }
}, true);
