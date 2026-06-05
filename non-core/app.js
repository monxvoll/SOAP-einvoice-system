/* WinMac OS 1.0 - App Logic
   E-Invoice System UI - Vanilla JS */

// Configuration
const SOAP_URL = 'http://localhost:8080/ws';
const SOAP_NS = 'http://einvoice.com/soap/gen';

// State
let selectedEmployee = JSON.parse(localStorage.getItem('selectedEmployee') || 'null');
let employeesCache = [];
let productsCache = [];
let customersCache = [];
let invoiceLines = []; // { productId, productName, unitPrice, quantity }

// Clock
function updateClock() {
  const now = new Date();
  const h = String(now.getHours()).padStart(2, '0');
  const m = String(now.getMinutes()).padStart(2, '0');
  const s = String(now.getSeconds()).padStart(2, '0');
  document.getElementById('clock').textContent = `${h}:${m}:${s}`;
}
setInterval(updateClock, 1000);
updateClock();

// Toast
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icons = { success: '✓', error: '✕', info: '⬥' };
  toast.innerHTML = `<span>${icons[type] || '⬥'}</span> ${message}`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// SOAP Helper
function buildSoapEnvelope(bodyXml) {
  return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
            xmlns:gen="${SOAP_NS}">
    <soapenv:Header/>
    <soapenv:Body>${bodyXml}</soapenv:Body>
  </soapenv:Envelope>`;
}

async function soapRequest(bodyXml) {
  const res = await fetch(SOAP_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/xml; charset=utf-8',
      'SOAPAction': '""'
    },
    body: buildSoapEnvelope(bodyXml)
  });
  const text = await res.text();
  const parser = new DOMParser();
  return parser.parseFromString(text, 'text/xml');
}

// XML Parsing Helpers
function getTagText(el, tag) {
  // Try with namespace prefix first
  const nodes = el.getElementsByTagNameNS(SOAP_NS, tag);
  if (nodes.length > 0) return nodes[0].textContent || '';
  // Fallback: without namespace
  const fallback = el.getElementsByTagName(tag);
  if (fallback.length > 0) return fallback[0].textContent || '';
  return '';
}

function parseEmployees(xmlDoc) {
  const items = xmlDoc.getElementsByTagNameNS(SOAP_NS, 'Employee');
  const arr = [];
  for (const el of items) {
    arr.push({
      id: getTagText(el, 'ID'),
      name: getTagText(el, 'Name'),
      phone: getTagText(el, 'phoneNumber'),
      email: getTagText(el, 'email'),
      activity: getTagText(el, 'productiveActivity')
    });
  }
  return arr;
}

function parseCustomers(xmlDoc) {
  const items = xmlDoc.getElementsByTagNameNS(SOAP_NS, 'Customer');
  const arr = [];
  for (const el of items) {
    arr.push({
      id: getTagText(el, 'ID'),
      name: getTagText(el, 'Name'),
      email: getTagText(el, 'email'),
      phone: getTagText(el, 'phoneNumber')
    });
  }
  return arr;
}

function parseProducts(xmlDoc) {
  const items = xmlDoc.getElementsByTagNameNS(SOAP_NS, 'Product');
  const arr = [];
  for (const el of items) {
    arr.push({
      id: getTagText(el, 'ID'),
      name: getTagText(el, 'name'),
      unitPrice: getTagText(el, 'unitPrice')
    });
  }
  return arr;
}

// Window Utilities
function createOverlay(id) {
  if (id && document.getElementById(id)) return null;
  const overlay = document.createElement('div');
  if (id) overlay.id = id;
  overlay.className = 'window-overlay';
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) closeOverlay(overlay);
  });
  return overlay;
}

function closeOverlay(overlay) {
  overlay.style.opacity = '0';
  overlay.style.transition = 'opacity 0.15s';
  setTimeout(() => overlay.remove(), 150);
}

function createWindow(title, icon, width) {
  const win = document.createElement('div');
  win.className = 'window';
  if (width) win.style.width = width;

  win.innerHTML = `
    <div class="window-titlebar">
      <div class="traffic-lights">
        <div class="traffic-light tl-close" data-action="close"></div>
        <div class="traffic-light tl-min"></div>
        <div class="traffic-light tl-max"></div>
      </div>
      <span class="title-text">${icon ? icon + ' ' : ''}${title}</span>
    </div>
  `;

  // Close button
  win.querySelector('.tl-close').addEventListener('click', () => {
    const overlay = win.closest('.window-overlay');
    if (overlay) closeOverlay(overlay);
  });

  return win;
}

// 1. EMPLOYEE SELECTOR WINDOW
async function openEmployeeWindow() {
  const overlay = createOverlay('overlay-employee');
  if (!overlay) return;
  const win = createWindow('Empleados — Selector', '', '520px');

  const body = document.createElement('div');
  body.className = 'window-body';
  body.innerHTML = '<div class="loading">Cargando empleados...</div>';
  win.appendChild(body);

  const statusbar = document.createElement('div');
  statusbar.className = 'window-statusbar';
  statusbar.innerHTML = selectedEmployee
    ? `<span>Seleccionado: <b>${selectedEmployee.name}</b></span>`
    : '<span>Ningún empleado seleccionado</span>';
  win.appendChild(statusbar);

  overlay.appendChild(win);
  document.body.appendChild(overlay);

  try {
    const xmlDoc = await soapRequest('<gen:EmployeeListRequest/>');
    employeesCache = parseEmployees(xmlDoc);

    if (employeesCache.length === 0) {
      body.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📭</div>
          <p>No se encontraron empleados</p>
        </div>`;
      return;
    }

    body.innerHTML = '';
    employeesCache.forEach(emp => {
      const card = document.createElement('div');
      card.className = 'employee-card' +
        (selectedEmployee && selectedEmployee.id === emp.id ? ' selected' : '');
      card.innerHTML = `
        <div class="emp-avatar">${emp.name.charAt(0)}</div>
        <div class="emp-info">
          <div class="emp-name">${emp.name}</div>
          <div class="emp-role">${emp.activity}</div>
          <div class="emp-role text-muted">${emp.email}</div>
        </div>
        <div class="emp-check">✓</div>
      `;
      card.addEventListener('click', () => {
        // Deselect previous
        body.querySelectorAll('.employee-card').forEach(c => c.classList.remove('selected'));
        // Select this
        card.classList.add('selected');
        selectedEmployee = emp;
        localStorage.setItem('selectedEmployee', JSON.stringify(emp));
        statusbar.innerHTML = `<span>Seleccionado: <b>${emp.name}</b></span>`;
        showToast(`Empleado: ${emp.name}`, 'success');
      });
      body.appendChild(card);
    });
  } catch (err) {
    body.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">⚠</div>
        <p>Error de conexión con el servidor SOAP</p>
        <p class="text-muted" style="font-size:14px;margin-top:8px;">${err.message}</p>
      </div>`;
  }
}

// 2. INVOICE FORM WINDOW (DIAN)
async function openInvoiceWindow() {
  const overlay = createOverlay('overlay-invoice');
  if (!overlay) return;
  const win = createWindow('Facturación Electrónica DIAN', '', '720px');

  // Toolbar
  const toolbar = document.createElement('div');
  toolbar.className = 'window-toolbar';
  toolbar.innerHTML = `
    <button class="toolbar-btn" id="btn-add-line"><i class="ph ph-plus"></i> Línea</button>
    <div class="toolbar-divider"></div>
    <button class="toolbar-btn" id="btn-clear-form"><i class="ph ph-eraser"></i> Limpiar</button>
  `;
  win.appendChild(toolbar);

  const body = document.createElement('div');
  body.className = 'window-body';
  body.innerHTML = '<div class="loading">Cargando catálogos...</div>';
  win.appendChild(body);

  const statusbar = document.createElement('div');
  statusbar.className = 'window-statusbar';
  statusbar.innerHTML = '<span>Listo</span><span id="inv-total">Total: $0</span>';
  win.appendChild(statusbar);

  overlay.appendChild(win);
  document.body.appendChild(overlay);

  try {
    // Load products & customers in parallel
    const [prodDoc, custDoc] = await Promise.all([
      soapRequest('<gen:ProductListRequest/>'),
      soapRequest('<gen:CustomerListRequest/>')
    ]);
    productsCache = parseProducts(prodDoc);
    customersCache = parseCustomers(custDoc);

    renderInvoiceForm(body, win);
  } catch (err) {
    body.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">⚠</div>
        <p>Error de conexión con el servidor SOAP</p>
        <p class="text-muted" style="font-size:14px;margin-top:8px;">${err.message}</p>
      </div>`;
  }
}

function renderInvoiceForm(body, win) {
  invoiceLines = [{ productId: '', productName: '', unitPrice: 0, quantity: 1 }];

  const emp = selectedEmployee;

  body.innerHTML = `
    <!-- DIAN Header -->
    <div class="invoice-header">
      <img src="assets/dian_logo.png" alt="DIAN Logo">
      <div class="invoice-title">
        <h3>Factura Electrónica de Venta</h3>
        <p>República de Colombia — DIAN</p>
      </div>
      <span class="badge">FE-${Date.now().toString().slice(-6)}</span>
    </div>

    <!-- Employee Section -->
    <div class="section-header">
      <i class="ph ph-identification-badge"></i> Datos del Empleado (Vendedor)
    </div>
    <div class="form-grid">
      <div>
        <label class="retro-label">ID</label>
        <input class="retro-input" id="inv-emp-id" value="${emp ? emp.id : ''}" ${emp ? 'readonly' : ''}>
      </div>
      <div>
        <label class="retro-label">Nombre</label>
        <input class="retro-input" id="inv-emp-name" value="${emp ? emp.name : ''}" ${emp ? 'readonly' : ''}>
      </div>
      <div>
        <label class="retro-label">Teléfono</label>
        <input class="retro-input" id="inv-emp-phone" value="${emp ? emp.phone : ''}" ${emp ? 'readonly' : ''}>
      </div>
      <div>
        <label class="retro-label">Email</label>
        <input class="retro-input" id="inv-emp-email" value="${emp ? emp.email : ''}" ${emp ? 'readonly' : ''}>
      </div>
      <div class="full-width">
        <label class="retro-label">Actividad Productiva</label>
        <input class="retro-input" id="inv-emp-activity" value="${emp ? emp.activity : ''}" ${emp ? 'readonly' : ''}>
      </div>
    </div>
    ${!emp ? '<p style="color:var(--danger);font-size:14px;margin-bottom:8px;">⚠ Selecciona un empleado primero desde la app "Empleados"</p>' : ''}

    <!-- Customer Section -->
    <div class="section-header">
      <i class="ph ph-buildings"></i> Datos del Cliente
    </div>
    <div class="form-grid">
      <div class="full-width">
        <label class="retro-label">Seleccionar Cliente</label>
        <select class="retro-select" id="inv-customer-select">
          <option value="">-- Seleccione un cliente --</option>
          ${customersCache.map(c => `<option value="${c.id}">${c.name} (${c.id})</option>`).join('')}
        </select>
      </div>
      <div>
        <label class="retro-label">ID (NIT)</label>
        <input class="retro-input" id="inv-cust-id">
      </div>
      <div>
        <label class="retro-label">Razón Social</label>
        <input class="retro-input" id="inv-cust-name">
      </div>
      <div>
        <label class="retro-label">Email</label>
        <input class="retro-input" id="inv-cust-email">
      </div>
      <div>
        <label class="retro-label">Teléfono</label>
        <input class="retro-input" id="inv-cust-phone">
      </div>
    </div>

    <!-- Products Section -->
    <div class="section-header">
      <i class="ph ph-package"></i> Productos
    </div>
    <div id="product-lines"></div>

    <div style="margin-top:16px; display:flex; gap:8px; justify-content:flex-end;">
      <button class="retro-btn" id="btn-send-invoice" style="display:flex;align-items:center;gap:6px;">
        <i class="ph ph-paper-plane-tilt"></i> Enviar Factura
      </button>
    </div>
  `;

  // Customer select handler
  const custSelect = body.querySelector('#inv-customer-select');
  custSelect.addEventListener('change', () => {
    const cust = customersCache.find(c => c.id === custSelect.value);
    body.querySelector('#inv-cust-id').value = cust ? cust.id : '';
    body.querySelector('#inv-cust-name').value = cust ? cust.name : '';
    body.querySelector('#inv-cust-email').value = cust ? cust.email : '';
    body.querySelector('#inv-cust-phone').value = cust ? cust.phone : '';
  });

  // Render product lines
  renderProductLines(body, win);

  // Add line button
  win.querySelector('#btn-add-line').addEventListener('click', () => {
    invoiceLines.push({ productId: '', productName: '', unitPrice: 0, quantity: 1 });
    renderProductLines(body, win);
  });

  // Clear form
  win.querySelector('#btn-clear-form').addEventListener('click', () => {
    renderInvoiceForm(body, win);
    showToast('Formulario limpiado', 'info');
  });

  // Send invoice
  body.querySelector('#btn-send-invoice').addEventListener('click', () => sendInvoice(body, win));
}

function renderProductLines(body, win) {
  const container = body.querySelector('#product-lines');
  container.innerHTML = '';

  invoiceLines.forEach((line, idx) => {
    const div = document.createElement('div');
    div.className = 'product-line';
    div.innerHTML = `
      <div class="pl-product">
        <label class="retro-label">Producto</label>
        <select class="retro-select product-select" data-idx="${idx}">
          <option value="">-- Seleccione --</option>
          ${productsCache.map(p => `<option value="${p.id}" ${line.productId === p.id ? 'selected' : ''}>${p.name}</option>`).join('')}
        </select>
      </div>
      <div class="pl-qty">
        <label class="retro-label">Cant.</label>
        <input class="retro-input qty-input" type="number" min="1" value="${line.quantity}" data-idx="${idx}">
      </div>
      <div class="pl-price">
        <label class="retro-label">P. Unit.</label>
        <input class="retro-input" type="text" value="${line.unitPrice ? '$' + Number(line.unitPrice).toLocaleString('es-CO') : ''}" readonly>
      </div>
      <button class="remove-line" data-idx="${idx}" title="Eliminar línea">✕</button>
    `;
    container.appendChild(div);

    // Product select
    div.querySelector('.product-select').addEventListener('change', (e) => {
      const prod = productsCache.find(p => p.id === e.target.value);
      invoiceLines[idx].productId = prod ? prod.id : '';
      invoiceLines[idx].productName = prod ? prod.name : '';
      invoiceLines[idx].unitPrice = prod ? prod.unitPrice : 0;
      // Update price input in the DOM directly
      const priceInput = div.querySelector('.pl-price .retro-input');
      if (priceInput) {
        priceInput.value = prod ? '$' + Number(prod.unitPrice).toLocaleString('es-CO') : '';
      }
      updateTotal(win);
    });

    // Quantity
    div.querySelector('.qty-input').addEventListener('change', (e) => {
      invoiceLines[idx].quantity = Math.max(1, parseInt(e.target.value) || 1);
      updateTotal(win);
    });

    // Remove
    div.querySelector('.remove-line').addEventListener('click', () => {
      if (invoiceLines.length > 1) {
        invoiceLines.splice(idx, 1);
        renderProductLines(body, win);
        updateTotal(win);
      }
    });

  });
  updateTotal(win);
}

function updateTotal(win) {
  const total = invoiceLines.reduce((sum, l) => sum + (Number(l.unitPrice) * l.quantity), 0);
  const el = win.querySelector('#inv-total');
  if (el) el.textContent = `Total: $${total.toLocaleString('es-CO')}`;
}

async function sendInvoice(body, win) {
  // Validate
  const empId = body.querySelector('#inv-emp-id').value;
  const empName = body.querySelector('#inv-emp-name').value;
  const empPhone = body.querySelector('#inv-emp-phone').value;
  const empEmail = body.querySelector('#inv-emp-email').value;
  const empActivity = body.querySelector('#inv-emp-activity').value;

  const custId = body.querySelector('#inv-cust-id').value;
  const custName = body.querySelector('#inv-cust-name').value;
  const custEmail = body.querySelector('#inv-cust-email').value;
  const custPhone = body.querySelector('#inv-cust-phone').value;

  if (!empId || !empName) {
    showToast('Selecciona un empleado primero', 'error');
    return;
  }
  if (!custId) {
    showToast('Selecciona un cliente', 'error');
    return;
  }

  const validLines = invoiceLines.filter(l => l.productId);
  if (validLines.length === 0) {
    showToast('Agrega al menos un producto', 'error');
    return;
  }

  // Build SOAP XML
  const productsXml = validLines.map(l => `
    <gen:Product>
      <gen:ID>${escapeXml(l.productId)}</gen:ID>
      <gen:name>${escapeXml(l.productName)}</gen:name>
      <gen:unitPrice>${escapeXml(String(l.unitPrice))}</gen:unitPrice>
    </gen:Product>`).join('');

  const quantitiesXml = validLines.map(l =>
    `<gen:quantities>${l.quantity}</gen:quantities>`).join('');

  const invoiceXml = `
    <gen:Einvoice>
      <gen:Employee>
        <gen:ID>${escapeXml(empId)}</gen:ID>
        <gen:Name>${escapeXml(empName)}</gen:Name>
        <gen:phoneNumber>${escapeXml(empPhone)}</gen:phoneNumber>
        <gen:email>${escapeXml(empEmail)}</gen:email>
        <gen:productiveActivity>${escapeXml(empActivity)}</gen:productiveActivity>
      </gen:Employee>
      <gen:Customer>
        <gen:ID>${escapeXml(custId)}</gen:ID>
        <gen:Name>${escapeXml(custName)}</gen:Name>
        <gen:email>${escapeXml(custEmail)}</gen:email>
        <gen:phoneNumber>${escapeXml(custPhone)}</gen:phoneNumber>
      </gen:Customer>
      ${productsXml}
      ${quantitiesXml}
    </gen:Einvoice>`;

  // Update status
  win.querySelector('.window-statusbar').innerHTML =
    '<span>Enviando factura...</span><span><div class="loading" style="padding:0;font-size:14px;">⏳</div></span>';

  try {
    const xmlDoc = await soapRequest(invoiceXml);
    const emailCheck = xmlDoc.getElementsByTagNameNS(SOAP_NS, 'emailCheck');
    const sent = emailCheck.length > 0 && emailCheck[0].textContent === 'true';

    if (sent) {
      showToast('¡Factura enviada exitosamente! ✉', 'success');
      win.querySelector('.window-statusbar').innerHTML =
        '<span style="color:var(--success);">✓ Factura enviada</span><span></span>';
    } else {
      showToast('Factura procesada, pero el email no pudo enviarse', 'error');
      win.querySelector('.window-statusbar').innerHTML =
        '<span style="color:var(--danger);">✕ Error en envío de email</span><span></span>';
    }
  } catch (err) {
    showToast('Error de conexión: ' + err.message, 'error');
    win.querySelector('.window-statusbar').innerHTML =
      '<span style="color:var(--danger);">✕ Error de conexión</span><span></span>';
  }
}

function escapeXml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

// 3. CATALOG WINDOW (Products + Clients)
async function openCatalogWindow() {
  const overlay = createOverlay('overlay-catalog');
  if (!overlay) return;
  const win = createWindow('Catálogo — Productos y Clientes', '', '750px');

  // Tabs
  const tabBar = document.createElement('div');
  tabBar.className = 'tab-bar';
  tabBar.innerHTML = `
    <button class="tab-btn active" data-tab="products"><i class="ph ph-package"></i> Productos</button>
    <button class="tab-btn" data-tab="customers"><i class="ph ph-address-book"></i> Clientes</button>
  `;
  win.appendChild(tabBar);

  const body = document.createElement('div');
  body.className = 'window-body';
  body.innerHTML = '<div class="loading">Cargando catálogos...</div>';
  win.appendChild(body);

  const statusbar = document.createElement('div');
  statusbar.className = 'window-statusbar';
  statusbar.innerHTML = '<span>Cargando...</span>';
  win.appendChild(statusbar);

  overlay.appendChild(win);
  document.body.appendChild(overlay);

  try {
    const [prodDoc, custDoc] = await Promise.all([
      soapRequest('<gen:ProductListRequest/>'),
      soapRequest('<gen:CustomerListRequest/>')
    ]);
    productsCache = parseProducts(prodDoc);
    customersCache = parseCustomers(custDoc);

    // Build tab contents
    body.innerHTML = `
      <div class="tab-content active" id="tab-products"></div>
      <div class="tab-content" id="tab-customers"></div>
    `;

    // Products table
    const prodTab = body.querySelector('#tab-products');
    if (productsCache.length === 0) {
      prodTab.innerHTML = '<div class="empty-state"><div class="empty-icon">📦</div><p>No hay productos</p></div>';
    } else {
      prodTab.innerHTML = `
        <table class="retro-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Precio Unitario</th>
            </tr>
          </thead>
          <tbody>
            ${productsCache.map(p => `
              <tr>
                <td><span class="badge">${p.id}</span></td>
                <td>${p.name}</td>
                <td>$${Number(p.unitPrice).toLocaleString('es-CO')}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    }

    // Customers table
    const custTab = body.querySelector('#tab-customers');
    if (customersCache.length === 0) {
      custTab.innerHTML = '<div class="empty-state"><div class="empty-icon">👤</div><p>No hay clientes</p></div>';
    } else {
      custTab.innerHTML = `
        <table class="retro-table">
          <thead>
            <tr>
              <th>NIT / ID</th>
              <th>Razón Social</th>
              <th>Email</th>
              <th>Teléfono</th>
            </tr>
          </thead>
          <tbody>
            ${customersCache.map(c => `
              <tr>
                <td><span class="badge">${c.id}</span></td>
                <td>${c.name}</td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    }

    statusbar.innerHTML = `<span>${productsCache.length} productos — ${customersCache.length} clientes</span>`;

    // Tab switching
    tabBar.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        tabBar.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        body.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        body.querySelector(`#tab-${btn.dataset.tab}`).classList.add('active');
      });
    });

  } catch (err) {
    body.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">⚠</div>
        <p>Error de conexión con el servidor SOAP</p>
        <p class="text-muted" style="font-size:14px;margin-top:8px;">${err.message}</p>
      </div>`;
    statusbar.innerHTML = '<span style="color:var(--danger);">Error</span>';
  }
}

// Desktop Icon Selection
document.querySelectorAll('.desktop-icon').forEach(icon => {
  icon.addEventListener('click', () => {
    document.querySelectorAll('.desktop-icon').forEach(i => i.classList.remove('selected'));
    icon.classList.add('selected');
  });
});

// Deselect on desktop click
document.getElementById('desktop-area').addEventListener('click', (e) => {
  if (e.target.id === 'desktop-area' || e.target.id === 'watermark') {
    document.querySelectorAll('.desktop-icon').forEach(i => i.classList.remove('selected'));
  }
});
