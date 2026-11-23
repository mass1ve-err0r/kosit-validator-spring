<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { ArrowDownToLine, BookOpen, Check, CheckCircle2, ChevronDown, CircleAlert, Code2, Copy, FileCode2, FileUp, LoaderCircle, RotateCcw, UploadCloud, XCircle, ReceiptEuro } from 'lucide-vue-next'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { apiExamples, apiLanguages } from '@/api-examples'

const buildInfo = ref(null)

const input = ref(null)
const file = ref(null)
const result = ref(null)
const loading = ref(false)
const dragging = ref(false)
const error = ref('')
const view = ref('preview')
const showApiGuide = ref(false)
const apiLanguage = ref('C (libcurl)')
const copiedExample = ref(false)
const copiedCommand = ref(false)
const localApiUrl = 'http://localhost:8090/api/validate'
const hostedApiUrl = 'https://de.e-invoicing.tools/api/validate'
const apiUrl = ref(localApiUrl)
const selectedApiUrl = computed(() => apiUrl.value.trim() || localApiUrl)
const currentExample = computed(() => {
    const example = apiExamples[apiLanguage.value]
    return { ...example, code: example.code.replaceAll(localApiUrl, selectedApiUrl.value) }
})

function selectApiUrl(url) {
    apiUrl.value = url
    copiedExample.value = false
}

const pdfUrl = ref('')
const html = computed(() => typeof result.value?.html === 'string' ? result.value.html : '')
const recommendation = computed(() => String(result.value?.recommendation ?? '').toUpperCase())
const status = computed(() => {
    if (!result.value) {
        return null
    }
    if (recommendation.value === 'ACCEPTABLE') {
        return { label: 'Akzeptiert', className: 'status-accepted', icon: CheckCircle2 }
    }
    if (recommendation.value === 'REJECT') {
        return { label: 'Abgelehnt', className: 'status-rejected', icon: XCircle }
    }
    return { label: 'Mit Hinweisen', className: 'status-warning', icon: CircleAlert }
})

function chooseFile() {
    input.value?.click()
}

function onChange(event) {
    const selected = event.target.files?.[0]
    event.target.value = ''
    if (selected) {
        selectFile(selected)
    }
}

function onDrop(event) {
    dragging.value = false
    const selected = event.dataTransfer?.files?.[0]
    if (selected) {
        selectFile(selected)
    }
}

function selectFile(selected) {
    if (loading.value) {
        return
    }
    if (!selected.name.toLowerCase().endsWith('.xml')) {
        error.value = 'Bitte eine XML-Datei auswählen.'
        return
    }
    file.value = selected
    result.value = null
    error.value = ''
    view.value = 'preview'
    validate()
}

async function validate() {
    if (!file.value || loading.value) {
        return
    }

    loading.value = true
    error.value = ''
    result.value = null

    try {
        const response = await axios.post('/api/validate', await file.value.arrayBuffer(), {
            headers: {
                'Content-Type': 'application/xml',
                'Accept': 'application/json'
            }
        })

        result.value = response.data
        if (!html.value) {
            error.value = 'Die Validierung hat keinen HTML-Bericht zurückgegeben.'
        }
    } catch (exception) {
        error.value = exception.response?.data?.message || exception.message || 'Validierung fehlgeschlagen.'
    } finally {
        loading.value = false
    }
}

async function copyExample() {
    try {
        await navigator.clipboard.writeText(currentExample.value.code)
        copiedExample.value = true
    } catch {
        copiedExample.value = false
    }
}

async function copyCommand() {
    try {
        await navigator.clipboard.writeText(currentExample.value.command)
        copiedCommand.value = true
    } catch {
        copiedCommand.value = false
    }
}

function downloadHtml() {
    if (!html.value) {
        return
    }
    const name = file.value?.name.replace(/\.xml$/i, '') || 'xrechnung'
    const url = URL.createObjectURL(new Blob([html.value], { type: 'text/html;charset=utf-8' }))
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `${name}-report.html`
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
}

function downloadPdf() {
  if (!pdfUrl.value) {
    return
  }

  const name = file.value?.name.replace(/\.xml$/i, '') || 'xrechnung'
  const anchor = document.createElement('a')
  anchor.href = pdfUrl.value
  anchor.download = `${name}-visualisiert.pdf`
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
}

function reset() {
    if (loading.value) {
        return
    }
    file.value = null
    result.value = null
    error.value = ''
    view.value = 'preview'
}

watch(() => result.value?.pdfBase64, (base64) => {
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value)
    pdfUrl.value = ''
  }

  if (!base64 || recommendation.value !== 'ACCEPTABLE') {
    return
  }

  try {
    const bytes = Uint8Array.from(atob(base64), character => character.charCodeAt(0))
    const signature = String.fromCharCode(...bytes.subarray(0, 5))

    if (signature !== '%PDF-') {
      return
    }

    pdfUrl.value = URL.createObjectURL(new Blob([bytes], { type: 'application/pdf' }))
  } catch {
    pdfUrl.value = ''
  }
})

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/system/build-info')
    buildInfo.value = data
  } catch {
    buildInfo.value = null
  }
})
onUnmounted(() => {
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value)
  }
})
</script>

<template>
    <div class="app-shell">
        <header class="topbar">
            <div class="topbar-inner">
                <div class="brand">
                    <div class="brand-icon"><ReceiptEuro :size="18" :stroke-width="1.7" /></div>
                    <span>XRechnung <span class="brand-muted">Toolbox</span></span>
                </div>
                <Button variant="ghost" size="sm" class="access-button" :aria-expanded="showApiGuide" aria-controls="api-guide" @click="showApiGuide = !showApiGuide">
                    <BookOpen :size="14" /> API-Anleitung <ChevronDown :size="13" :class="{ 'guide-chevron-open': showApiGuide }" />
                </Button>
            </div>
        </header>

        <section v-if="showApiGuide" id="api-guide" class="api-guide">
            <div class="api-guide-heading">
                <div>
                    <p class="eyebrow">ÖFFENTLICHE API</p>
                    <h2>Anleitung zur API</h2>
                </div>
                <label class="api-language-picker">
                    <span>Sprache</span>
                    <select v-model="apiLanguage" @change="copiedExample = false; copiedCommand = false">
                        <option v-for="language in apiLanguages" :key="language" :value="language">{{ language }}</option>
                    </select>
                </label>
            </div>
            <div class="api-url-row">
                <label for="api-url">API-Endpunkt</label>
                <div class="api-url-controls">
                    <input id="api-url" v-model="apiUrl" type="url" spellcheck="false" autocomplete="off" @input="copiedExample = false" />
                    <div class="api-url-presets">
                        <button type="button" :class="{ active: selectedApiUrl === localApiUrl }" @click="selectApiUrl(localApiUrl)">Lokal</button>
                        <button type="button" :class="{ active: selectedApiUrl === hostedApiUrl }" @click="selectApiUrl(hostedApiUrl)">Public</button>
                    </div>
                </div>
            </div>
            <div class="api-endpoints">
                <span><strong>POST</strong> {{ selectedApiUrl }} <em>→ JSON</em></span>
              <span>| Content-Type: application/xml</span>
            </div>
            <div class="api-endpoints">
              <span><strong>POST</strong> {{ selectedApiUrl.replace(/\/$/, '') }}/asHTML <em>→ HTML (Falls erfolgreich validiert)</em></span>
              <span>| Content-Type: application/xml</span>
            </div>
            <div class="api-code-heading">
                <span>{{ currentExample.filename }}</span>
                <Button variant="ghost" size="sm" class="copy-example-button" @click="copyExample">
                    <Check v-if="copiedExample" :size="14" />
                    <Copy v-else :size="14" />
                    {{ copiedExample ? 'Kopiert' : 'Code kopieren' }}
                </Button>
            </div>
            <pre class="api-code"><code>{{ currentExample.code }}</code></pre>
            <div class="api-code-heading api-command-heading">
                <span>{{ apiLanguage === 'Python' ? 'Ausführen' : 'Kompilieren & ausführen' }}</span>
                <Button variant="ghost" size="sm" class="copy-example-button" @click="copyCommand">
                    <Check v-if="copiedCommand" :size="14" />
                    <Copy v-else :size="14" />
                    {{ copiedCommand ? 'Kopiert' : 'Befehl kopieren' }}
                </Button>
            </div>
            <pre class="api-code api-command"><code>{{ currentExample.command }}</code></pre>
        </section>

        <main class="workspace">
            <section class="left-pane">
                <div class="intro">
                    <p class="eyebrow">DOKUMENTENPRÜFUNG & VISUALISIERUNG FÜR</p>
                    <h1>XRechnung</h1>
                    <p>XML-Datei auswählen und den vollständigen KoSIT-Prüfbericht direkt ansehen.<br>Bei Erfolg wird ebenfalls ein Druckbild erstellt.</p>
                </div>

                <div
                    class="dropzone"
                    :class="{ 'dropzone-active': dragging, 'dropzone-disabled': loading }"
                    role="button"
                    tabindex="0"
                    aria-label="XML-Datei auswählen"
                    @click="chooseFile"
                    @keydown.enter.prevent="chooseFile"
                    @keydown.space.prevent="chooseFile"
                    @dragover.prevent="dragging = true"
                    @dragleave.prevent="dragging = false"
                    @drop.prevent="onDrop"
                >
                    <div class="upload-icon"><UploadCloud :size="21" :stroke-width="1.6" /></div>
                    <div class="upload-heading">XML-Datei hier ablegen</div>
                    <p>oder klicken, um eine XRechnung auszuwählen</p>
                    <span class="upload-format">.xml</span>
                </div>
                <input ref="input" type="file" accept=".xml,application/xml,text/xml" class="sr-only" @change="onChange" />

                <div v-if="file" class="file-row">
                    <FileCode2 :size="18" class="file-icon" />
                    <div class="file-details">
                        <strong :title="file.name">{{ file.name }}</strong>
                        <span>{{ (file.size / 1024).toFixed(1) }} KB · XML</span>
                    </div>
                    <Button variant="ghost" size="icon" :disabled="loading" aria-label="Datei entfernen" @click="reset">
                        <RotateCcw :size="15" />
                    </Button>
                </div>

                <div v-if="loading" class="notice loading-notice">
                    <LoaderCircle :size="16" class="animate-spin" /> Validierung läuft …
                </div>

                <div v-if="result && status" class="result-box" :class="status.className">
                    <div class="result-head">
                        <component :is="status.icon" :size="17" />
                        <strong>{{ status.label }}</strong>
                        <Badge variant="outline" class="result-badge">{{ recommendation }}</Badge>
                    </div>
                    <p>{{ result.processingSuccessful ? 'Die Validierung wurde abgeschlossen.' : 'Bei der Verarbeitung sind Fehler aufgetreten.' }}</p>
                </div>

                <div v-if="error" class="notice error-notice" role="alert"><CircleAlert :size="16" /> {{ error }}</div>

                <Button v-if="file && !loading" variant="outline" class="retry-button" @click="validate">
                    Erneut prüfen
                </Button>
            </section>

            <section class="right-pane">
                <div class="report-header">
                    <div>
                        <p class="eyebrow">VALIDIERUNGSBERICHT</p>
                        <h2>Ergebnis</h2>
                    </div>
                  <div class="report-actions">
                    <Button variant="outline" size="sm" :disabled="!html" @click="downloadHtml">
                      <ArrowDownToLine :size="15" /> HTML
                    </Button>

                    <Button v-if="pdfUrl" variant="outline" size="sm" @click="downloadPdf">
                      <ArrowDownToLine :size="15" /> PDF
                    </Button>
                  </div>
                </div>

                <div class="report-surface">
                    <Tabs v-if="html" v-model="view" class="report-tabs">
                        <div class="tabs-top">
                            <TabsList class="tabs-list">
                                <TabsTrigger value="preview">Vorschau</TabsTrigger>
                                <TabsTrigger value="source"><Code2 :size="13" /> Quelltext</TabsTrigger>
                                <TabsTrigger v-if="pdfUrl" value="pdf">PDF Visualisierung</TabsTrigger>
                            </TabsList>
                            <span class="report-name">{{ file?.name }}</span>
                        </div>
                        <TabsContent value="preview" class="report-content">
                            <iframe class="report-frame" :srcdoc="html" sandbox="" title="KoSIT Validierungsbericht" referrerpolicy="no-referrer" />
                        </TabsContent>
                        <TabsContent value="source" class="report-content source-content">
                            <pre class="html-source">{{ html }}</pre>
                        </TabsContent>
                        <TabsContent v-if="pdfUrl" value="pdf" class="report-content">
                          <iframe
                              class="report-frame"
                              :src="pdfUrl"
                              title="XRechnung Visualisierung"
                          />
                        </TabsContent>
                    </Tabs>
                  <div v-else-if="loading" class="empty-preview" role="status" aria-live="polite">
                    <LoaderCircle :size="28" class="animate-spin" :stroke-width="1.5" />
                    <strong>XRechnung wird validiert …</strong>
                    <p>Bitte einen Moment. Der Prüfbericht wird erstellt.</p>
                  </div>

                    <div v-else class="empty-preview">
                        <div class="empty-preview-icon"><FileUp :size="24" :stroke-width="1.4" /></div>
                        <strong>Noch kein Bericht</strong>
                        <p>Nach dem Hochladen erscheint hier der vollständige HTML-Prüfbericht.</p>
                    </div>
                </div>
            </section>
        </main>

      <footer class="site-footer">
        <div class="site-footer-inner">
          <div class="site-footer-meta">
            <span>
                © 2026 - {{ new Date().getFullYear() }} ·
                Ein Service von
                <a href="https://baig.software" target="_blank" rel="noopener noreferrer">Baig Software</a>
            </span>

            <span v-if="buildInfo" class="build-info">
                Version {{ buildInfo.version }} · Build {{ buildInfo.commitHash?.slice(0, 8) || 'unknown' }}
            </span>
          </div>

          <span>Kein offizieller Dienst der KoSIT.</span>
        </div>
      </footer>
    </div>
</template>
