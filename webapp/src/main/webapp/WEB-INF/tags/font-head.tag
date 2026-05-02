<%@ tag language="java" pageEncoding="UTF-8" %>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
<script>
(function () {
    document.addEventListener('keydown', function (e) {
        if (e.target.matches('input[type="number"]') && ['e', 'E', '+', '-'].includes(e.key)) {
            e.preventDefault();
        }
    }, true);

    document.addEventListener('input', function (e) {
        var el = e.target;
        if (el.matches('input[type="number"]') && el.max) {
            var maxLen = String(el.max).length;
            if (el.value.length > maxLen) {
                el.value = el.value.slice(0, maxLen);
            }
        }
    }, true);
}());
</script>
