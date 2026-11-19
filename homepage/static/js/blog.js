/* Blog behaviour: shared nav, reveal, reading progress, code copy. */
(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  var nav = $('#nav'), burger = $('#burger'), links = $('#navLinks');
  function onScroll() {
    nav.classList.toggle('scrolled', window.scrollY > 12);
    var bar = $('#readProgress'), art = $('.prose');
    if (bar && art) {
      var r = art.getBoundingClientRect(), total = r.height - window.innerHeight * 0.5;
      bar.style.width = Math.max(0, Math.min(1, -r.top / Math.max(total, 1))) * 100 + '%';
    }
  }
  onScroll();
  window.addEventListener('scroll', onScroll, { passive: true });
  burger.addEventListener('click', function () {
    var open = links.classList.toggle('open');
    burger.setAttribute('aria-expanded', open);
  });

  var spot = $('#spot');
  if (!reduced && spot && window.matchMedia('(pointer:fine)').matches) {
    window.addEventListener('pointermove', function (e) {
      spot.style.left = e.clientX + 'px';
      spot.style.top = e.clientY + 'px';
    }, { passive: true });
  }

  var rev = $$('.reveal');
  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (es) {
      es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
    }, { threshold: 0.08 });
    rev.forEach(function (el, i) { el.style.transitionDelay = (i % 3) * 70 + 'ms'; io.observe(el); });
  } else {
    rev.forEach(function (el) { el.classList.add('in'); });
  }

  /* copy buttons on code blocks */
  $$('.prose pre').forEach(function (pre) {
    var host = pre.parentNode;
    if (!host.classList.contains('highlight')) {
      host = document.createElement('div');
      host.className = 'highlight';
      pre.parentNode.insertBefore(host, pre);
      host.appendChild(pre);
    }
    var b = document.createElement('button');
    b.type = 'button';
    b.className = 'copy-code';
    b.textContent = 'Copy';
    b.addEventListener('click', function () {
      if (!navigator.clipboard) return;
      navigator.clipboard.writeText(pre.innerText).then(function () {
        b.textContent = 'Copied';
        setTimeout(function () { b.textContent = 'Copy'; }, 1600);
      }, function () {});
    });
    host.appendChild(b);
  });

  /* star count */
  var el = $('#ghStars');
  if (el && window.fetch) {
    var cached = null;
    try { cached = JSON.parse(sessionStorage.getItem('lt_gh') || '{}').stars; } catch (e) {}
    var show = function (n) {
      el.textContent = n >= 1000 ? (n / 1000).toFixed(1).replace('.0', '') + 'k' : n;
      el.hidden = false;
    };
    if (cached) {
      show(cached);
    } else {
      fetch('https://api.github.com/repos/ferranpons/Llamatik')
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (j) { if (j && j.stargazers_count) show(j.stargazers_count); })
        .catch(function () {});
    }
  }
})();
