document.addEventListener('DOMContentLoaded', () => {
    const elements = {
        latestBlock: document.getElementById('latest-block'),
        txObserved: document.getElementById('tx-observed'),
        ethVolume: document.getElementById('eth-volume'),
        activeWallets: document.getElementById('active-wallets'),
        repeatedEntities: document.getElementById('repeated-entities'),
        baseGas: document.getElementById('base-gas'),
        rpcLatency: document.getElementById('rpc-latency'),
        networkStatus: document.getElementById('network-status'),
        summaryText: document.getElementById('summary-text'),
        txBody: document.getElementById('transaction-table-body'),
        repeatedPanel: document.getElementById('repeated-activity-panel'),
        graphPanel: document.getElementById('graph-panel')
    };

    async function fetchJson(url) {
        const response = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!response.ok) {
            throw new Error('Request failed: ' + response.status);
        }
        return response.json();
    }

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>'"]/g, (character) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
        }[character]));
    }

    function formatEth(value) {
        if (!value && value !== 0) return '0 ETH';
        const numeric = Number(value);
        return Number.isFinite(numeric) ? `${numeric.toFixed(4)} ETH` : String(value);
    }

    function renderStats(stats) {
        if (!stats) return;
        elements.latestBlock.textContent = stats.latestBlock ?? '--';
        elements.txObserved.textContent = stats.transactionsObserved ?? '--';
        elements.ethVolume.textContent = formatEth(stats.ethVolume ?? 0);
        elements.activeWallets.textContent = stats.uniqueWallets ?? '--';
        elements.repeatedEntities.textContent = stats.repeatedEntities ?? '--';
        elements.baseGas.textContent = stats.baseGas ?? '--';
        elements.rpcLatency.textContent = `${stats.rpcLatencyMs ?? 0} ms`;
        elements.networkStatus.textContent = stats.networkStatus ?? 'LIVE';
        document.getElementById('counter-transactions').textContent = String(stats.transactionsObserved ?? 0).padStart(7, '0');
        document.getElementById('counter-wallets').textContent = String(stats.uniqueWallets ?? 0).padStart(7, '0');
        document.getElementById('counter-blocks').textContent = String(stats.blocksObserved ?? 0).padStart(7, '0');

        const summary = stats.transactionsObserved && stats.blocksObserved
            ? `The current observation session has analyzed ${stats.transactionsObserved} transactions across ${stats.blocksObserved} blocks and observed ${stats.uniqueWallets} unique wallet addresses.`
            : 'Insufficient observations for a meaningful summary.';
        elements.summaryText.textContent = summary;
    }

    function renderTransactions(items) {
        if (!Array.isArray(items) || !elements.txBody) return;
        elements.txBody.innerHTML = '';
        items.forEach((tx) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(new Date(tx.timestamp).toLocaleTimeString())}</td>
                <td>${escapeHtml((tx.from || '').slice(0, 10))}...</td>
                <td>${escapeHtml((tx.to || tx.contract || '').slice(0, 12))}...</td>
                <td>${escapeHtml(formatEth(tx.value || 0))}</td>
                <td>${escapeHtml(tx.gasPrice || '0')} wei</td>
                <td>${escapeHtml(tx.blockNumber || '--')}</td>
                <td>${escapeHtml((tx.hash || '').slice(0, 12))}...</td>
            `;
            elements.txBody.appendChild(row);
        });
    }

    function renderRepeated(items) {
        if (!Array.isArray(items) || !elements.repeatedPanel) return;
        elements.repeatedPanel.innerHTML = items.length
            ? items.map(item => `<div><strong>${escapeHtml(item.address)}</strong><br/>${escapeHtml(item.transactionCount)} txns</div>`).join('<br/>')
            : '<div>No repeated activity detected.</div>';
    }

    function renderGraph(graph) {
        if (!graph || !elements.graphPanel) return;
        const nodes = graph.nodes || [];
        const edges = graph.edges || [];
        const width = 760;
        const columns = 10;
        const rowHeight = 76;
        const rows = Math.max(1, Math.ceil(nodes.length / columns));
        const height = Math.max(360, rows * rowHeight + 40);
        const positions = new Map(nodes.map((node, index) => {
            const column = index % columns;
            const row = Math.floor(index / columns);
            return [node.id, { x: 42 + column * ((width - 84) / (columns - 1)), y: 28 + row * rowHeight }];
        }));
        const visibleEdges = edges.filter(edge => positions.has(edge.source) && positions.has(edge.target)).slice(0, 80);
        const edgeMarkup = visibleEdges.map(edge => {
            const source = positions.get(edge.source);
            const target = positions.get(edge.target);
            return `<line class="graph-edge" x1="${source.x}" y1="${source.y}" x2="${target.x}" y2="${target.y}" stroke-width="${Math.min(5, 1 + Number(edge.transactionCount || 1))}" />`;
        }).join('');
        const nodeMarkup = nodes.map((node, index) => {
            const position = positions.get(node.id);
            const radius = Math.min(22, 8 + Math.sqrt(Number(node.transactionCount || 1)) * 2);
            const color = node.isRepeated ? '#db2777' : ['#7c3aed', '#0ea5e9', '#10b981', '#f59e0b'][index % 4];
            return `<g class="graph-node" tabindex="0" role="button" data-node-id="${escapeHtml(node.id)}" aria-label="Inspect ${escapeHtml(node.id)}"><circle cx="${position.x}" cy="${position.y}" r="${radius}" fill="${color}"/><text x="${position.x}" y="${position.y + radius + 15}" text-anchor="middle">${escapeHtml(node.id.slice(0, 8))}...</text><title>${escapeHtml(node.id)}</title></g>`;
        }).join('');
        elements.graphPanel.innerHTML = `<div class="graph-meta"><strong>${nodes.length} nodes</strong><span>${edges.length} connections</span><span>Block ${escapeHtml(graph.blockNumber ?? '--')}</span></div><div class="graph-toolbar" role="toolbar" aria-label="Graph controls"><button type="button" class="graph-control" data-graph-action="zoom-out" title="Zoom out" aria-label="Zoom out">−</button><output class="graph-zoom-value" id="graph-zoom-value">100%</output><button type="button" class="graph-control" data-graph-action="zoom-in" title="Zoom in" aria-label="Zoom in">+</button><button type="button" class="graph-control graph-reset" data-graph-action="reset" title="Reset view" aria-label="Reset graph view">Reset</button><span class="graph-help">Scroll to zoom · drag empty space to pan</span></div><div class="graph-viewport"><svg class="graph-canvas" viewBox="0 0 ${width} ${height}" preserveAspectRatio="xMidYMin meet" aria-label="Transaction network graph"><defs><filter id="graph-glow"><feGaussianBlur stdDeviation="3" result="blur"/><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs><g class="graph-world"><g class="graph-edges">${edgeMarkup}</g><g filter="url(#graph-glow)">${nodeMarkup}</g></g></svg></div><div class="graph-inspector" id="graph-inspector" aria-live="polite"><strong>Select a node</strong><span>Click a wallet or contract to inspect its activity.</span></div>`;
        const nodeById = new Map(nodes.map(node => [node.id, node]));
        const inspector = elements.graphPanel.querySelector('#graph-inspector');
        const world = elements.graphPanel.querySelector('.graph-world');
        const zoomValue = elements.graphPanel.querySelector('#graph-zoom-value');
        const view = { scale: 1, x: 0, y: 0, dragging: false, startX: 0, startY: 0 };
        const applyView = () => {
            world.setAttribute('transform', `translate(${view.x} ${view.y}) scale(${view.scale})`);
            zoomValue.textContent = `${Math.round(view.scale * 100)}%`;
        };
        const changeZoom = (amount) => {
            view.scale = Math.min(3, Math.max(0.65, view.scale + amount));
            applyView();
        };
        const inspectNode = (nodeId) => {
            const node = nodeById.get(nodeId);
            if (!node || !inspector) return;
            elements.graphPanel.querySelectorAll('.graph-node.is-selected').forEach(selected => selected.classList.remove('is-selected'));
            const selected = elements.graphPanel.querySelector(`[data-node-id="${CSS.escape(nodeId)}"]`);
            if (selected) selected.classList.add('is-selected');
            inspector.innerHTML = `<strong>${escapeHtml(node.id)}</strong><span>${escapeHtml(node.transactionCount)} transactions · ${escapeHtml(node.sentCount)} sent · ${escapeHtml(node.receivedCount)} received</span><span>${node.isRepeated ? 'Repeated activity detected' : 'No repeated activity threshold reached'}</span>`;
        };
        elements.graphPanel.querySelectorAll('.graph-node').forEach(nodeElement => {
            nodeElement.addEventListener('click', () => inspectNode(nodeElement.dataset.nodeId));
            nodeElement.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    inspectNode(nodeElement.dataset.nodeId);
                }
            });
        });
        elements.graphPanel.querySelectorAll('[data-graph-action]').forEach(control => {
            control.addEventListener('click', () => {
                const action = control.dataset.graphAction;
                if (action === 'zoom-in') changeZoom(0.2);
                if (action === 'zoom-out') changeZoom(-0.2);
                if (action === 'reset') {
                    view.scale = 1;
                    view.x = 0;
                    view.y = 0;
                    applyView();
                }
            });
        });
        const svg = elements.graphPanel.querySelector('.graph-canvas');
        svg.addEventListener('wheel', (event) => {
            event.preventDefault();
            changeZoom(event.deltaY < 0 ? 0.1 : -0.1);
        }, { passive: false });
        svg.addEventListener('pointerdown', (event) => {
            if (event.target.closest('.graph-node')) return;
            view.dragging = true;
            view.startX = event.clientX - view.x;
            view.startY = event.clientY - view.y;
            svg.setPointerCapture(event.pointerId);
            svg.classList.add('is-panning');
        });
        svg.addEventListener('pointermove', (event) => {
            if (!view.dragging) return;
            view.x = event.clientX - view.startX;
            view.y = event.clientY - view.startY;
            applyView();
        });
        const stopPanning = (event) => {
            if (!view.dragging) return;
            view.dragging = false;
            svg.releasePointerCapture?.(event.pointerId);
            svg.classList.remove('is-panning');
        };
        svg.addEventListener('pointerup', stopPanning);
        svg.addEventListener('pointercancel', stopPanning);
        applyView();
    }

    async function loadDashboard() {
        try {
            const [stats, txs, repeated, graph] = await Promise.all([
                fetchJson('/api/stats'),
                fetchJson('/api/transactions?size=8'),
                fetchJson('/api/repeated'),
                fetchJson('/api/graph')
            ]);
            renderStats(stats);
            renderTransactions(txs.content || []);
            renderRepeated(repeated || []);
            renderGraph(graph || { nodes: [], edges: [], blockNumber: '-' });
        } catch (error) {
            console.error(error);
            elements.summaryText.textContent = 'Unable to reach the observatory API.';
        }
    }

    async function searchObservatory() {
        const input = document.getElementById('search-input');
        const value = input.value.trim();
        if (!value) return;
        try {
            const result = await fetchJson('/api/search?q=' + encodeURIComponent(value));
            const transactions = result.transactions || [];
            renderTransactions(transactions);
            const addressCount = (result.addresses || []).length;
            const blockCount = (result.blocks || []).length;
            elements.summaryText.textContent = `Search for ${value} returned ${transactions.length} transactions, ${addressCount} addresses, and ${blockCount} blocks.`;
        } catch (error) {
            elements.summaryText.textContent = 'Search request failed.';
        }
    }

    function connectLiveUpdates() {
        if (!window.SockJS || !window.Stomp) return;
        const socket = new window.SockJS('/ws');
        const client = window.Stomp.over(socket);
        client.debug = () => {};
        client.connect({}, () => {
            client.subscribe('/topic/status', (message) => {
                try {
                    const event = JSON.parse(message.body);
                    if (event.type === 'BLOCK_PROCESSED') loadDashboard();
                } catch (error) {
                    console.warn('Unable to parse live update', error);
                }
            });
        }, () => {
            window.setTimeout(connectLiveUpdates, 10000);
        });
    }

    document.getElementById('search-button').addEventListener('click', searchObservatory);
    document.getElementById('search-input').addEventListener('keydown', (event) => {
        if (event.key === 'Enter') searchObservatory();
    });

    loadDashboard();
    connectLiveUpdates();
    window.setInterval(loadDashboard, 5000);
});
