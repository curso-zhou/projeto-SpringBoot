(function(){
  const KEY = 'alerts.mode';
  // Sempre usar modal por padrão
  function getMode(){ return localStorage.getItem(KEY) || 'modal'; }
  function setMode(mode){ localStorage.setItem(KEY, mode); }
  // Migração: se alguma vez ficou como 'toast', normalizar para 'modal'
  try { if (localStorage.getItem(KEY) === 'toast') { localStorage.setItem(KEY, 'modal'); } } catch {}
  function fallbackModal(icon, title, text, withCancel){
    return new Promise((resolve)=>{
      const bd = document.createElement('div');
      bd.className = 'alex-modal-backdrop';
      const panel = document.createElement('div');
      panel.className = 'alex-modal-panel';
      panel.innerHTML = '<div class="alex-modal-header"><div class="alex-modal-title"></div><button class="icon-btn" aria-label="Fechar">✕</button></div><div class="alex-modal-body"></div><div class="alex-modal-footer"></div>';
      panel.querySelector('.alex-modal-title').textContent = title || '';
      panel.querySelector('.alex-modal-body').textContent = text || '';
      const actions = panel.querySelector('.alex-modal-footer');
      const btnOk = document.createElement('button'); btnOk.className = 'alex-btn'; btnOk.textContent = 'OK';
      const btnCancel = document.createElement('button'); btnCancel.className = 'alex-btn-secondary'; btnCancel.textContent = 'Cancelar';
      actions.appendChild(btnOk);
      if (withCancel) actions.appendChild(btnCancel);
      btnOk.addEventListener('click', ()=>{ document.body.removeChild(bd); resolve(true); });
      btnCancel.addEventListener('click', ()=>{ document.body.removeChild(bd); resolve(false); });
      panel.querySelector('.icon-btn').addEventListener('click', ()=>{ document.body.removeChild(bd); resolve(false); });
      bd.appendChild(panel);
      document.body.appendChild(bd);
    });
  }
  function makeModal(icon, title, text){
    if (!window.Swal) { return fallbackModal(icon, title, text, false); }
    return Swal.fire({
      icon,
      title,
      text,
      confirmButtonText: 'OK',
      customClass: {
        popup: 'alex-swal-popup',
        title: 'alex-swal-title',
        actions: 'alex-swal-actions',
        confirmButton: 'alex-btn',
        cancelButton: 'alex-btn-secondary'
      },
      buttonsStyling: false
    });
  }
  const ui = {
    get mode(){ return getMode(); },
    set mode(v){ setMode(v); },
    // Toggle passa a ser inócuo: sempre retorna 'modal'
    toggle(){ setMode('modal'); return 'modal'; },
    // Sempre usar modal central
    success(msg, detail){ return makeModal('success', msg, detail); },
    error(msg, detail){ return makeModal('error', msg, detail); },
    info(msg, detail){ return makeModal('info', msg, detail); },
    warn(msg, detail){ return makeModal('warning', msg, detail); },
    async confirm(title, text){
      if (!window.Swal) { return fallbackModal('question', title, text, true); }
      const res = await Swal.fire({
        title,
        text,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Confirmar',
        cancelButtonText: 'Cancelar',
        customClass: {
          popup: 'alex-swal-popup',
          title: 'alex-swal-title',
          actions: 'alex-swal-actions',
          confirmButton: 'alex-btn',
          cancelButton: 'alex-btn-secondary'
        },
        buttonsStyling: false
      });
      return !!res.isConfirmed;
    }
  };
  window.ui = ui;
})();
