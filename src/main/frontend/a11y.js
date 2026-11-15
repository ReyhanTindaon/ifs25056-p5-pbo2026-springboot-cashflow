/*
 * Vaadin menambahkan <vaadin-connection-indicator> langsung ke <body>, di luar
 * landmark mana pun. Elemen itu berisi pesan status "Online"/"Offline" untuk
 * pembaca layar, jadi beri peran landmark agar kontennya tetap aksesibel.
 */
(() => {
  const mark = () => {
    const el = document.querySelector('vaadin-connection-indicator');
    if (el && !el.hasAttribute('role')) {
      el.setAttribute('role', 'complementary');
      el.setAttribute('aria-label', 'Status koneksi');
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', mark);
  } else {
    mark();
  }
  new MutationObserver(mark).observe(document.documentElement, { childList: true, subtree: true });
})();
