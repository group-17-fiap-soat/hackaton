document.addEventListener('DOMContentLoaded', () => {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const uploadQueueDiv = document.getElementById('upload-queue');
    const processBtn = document.getElementById('process-btn');
    const processedListDiv = document.getElementById('processed-list');

    let filesToProcess = [];
    const API_BASE_URL = '/api';

    function formatFileSize(bytes) {
        if (!bytes || bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
    }

    function formatDate(dateString) {
        if (!dateString) return '';
        return new Date(dateString).toLocaleString('pt-BR');
    }

    dropZone.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', (e) => handleFiles(e.target.files));

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, e => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => dropZone.addEventListener(eventName, () => dropZone.classList.add('drag-over'), false));
    ['dragleave', 'drop'].forEach(eventName => dropZone.addEventListener(eventName, () => dropZone.classList.remove('drag-over'), false));

    dropZone.addEventListener('drop', (e) => handleFiles(e.dataTransfer.files), false);

    function handleFiles(files) {
        [...files].forEach(file => {
            if (file.type.startsWith('video/')) {
                if (!filesToProcess.some(f => f.name === file.name)) {
                    filesToProcess.push(file);
                }
            }
        });
        updateQueueUI();
    }

    function updateQueueUI() {
        if (filesToProcess.length === 0) {
            uploadQueueDiv.innerHTML = '<p class="empty-message">A fila está vazia.</p>';
            processBtn.disabled = true;
        } else {
            uploadQueueDiv.innerHTML = filesToProcess.map(file => `
                <div class="file-item">
                    <span class="file-name">${file.name}</span>
                </div>
            `).join('');
            processBtn.disabled = false;
        }
    }

    function addProcessedFileToUI(processedFile) {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';
        const downloadUrl = `${API_BASE_URL}/download/${processedFile.zipPath}`;

        fileItem.innerHTML = `
            <div class="file-info">
                <span class="file-name">${processedFile.originalVideoPath}</span>
                <span class="file-details">
                    ${formatFileSize(processedFile.fileSize)} | ${formatDate(processedFile.uploadedAt)}
                </span>
            </div>
            <a href="${downloadUrl}" class="download-btn" download>⬇️ Download</a>
        `;
        processedListDiv.prepend(fileItem);
    }

    processBtn.addEventListener('click', async () => {
        if (filesToProcess.length === 0) return;

        processBtn.disabled = true;
        processBtn.textContent = 'Processando...';

        const files = [...filesToProcess];
        filesToProcess = [];
        updateQueueUI();

        for (const file of files) {
            try {
                const formData = new FormData();
                formData.append('video', file);

                const response = await fetch(`${API_BASE_URL}/upload`, {
                    method: 'POST',
                    body: formData
                });

                if (!response.ok) {
                    throw new Error(`Erro do servidor: ${response.statusText}`);
                }

            } catch (error) {
                console.error(`Falha na requisição para ${file.name}:`, error);
            }
        }

        await loadProcessedFiles();
        processBtn.textContent = '🚀 Processar Arquivos';
    });

    async function loadProcessedFiles() {
        try {
            const response = await fetch(`${API_BASE_URL}/status`);
            if (!response.ok) throw new Error('Falha ao carregar a lista de arquivos.');

            const data = await response.json();
            processedListDiv.innerHTML = '';

            if (data && data.length > 0) {
                data.forEach(file => {
                    addProcessedFileToUI(file);
                });
            } else {
                processedListDiv.innerHTML = '<p class="empty-message">Nenhum arquivo processado anteriormente.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar arquivos processados:', error);
            processedListDiv.innerHTML = '<p class="empty-message error-message">Não foi possível carregar a lista de arquivos.</p>';
        }
    }

    updateQueueUI();
    loadProcessedFiles();
});