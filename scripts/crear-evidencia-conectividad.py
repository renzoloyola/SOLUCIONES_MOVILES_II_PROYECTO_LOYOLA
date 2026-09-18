"""PDF de los puntos 2, 3 y 5 a partir del codigo y los resultados reales."""
from pathlib import Path
from xml.sax.saxutils import escape
import xml.etree.ElementTree as ET
import sys, textwrap

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'tmp/pdf-deps'))
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Preformatted, PageBreak, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
import pymupdf as fitz

OUT = ROOT / 'output/conectividad'
suite = ET.parse(OUT / 'resultados.xml').getroot()
assert suite.attrib['tests'] == '2' and suite.attrib['failures'] == '0' and suite.attrib['errors'] == '0'
styles = getSampleStyleSheet()
styles.add(ParagraphStyle('Codigo', fontName='Courier', fontSize=7.5, leading=10))
styles['Title'].textColor = colors.HexColor('#153C56')
styles['BodyText'].leading = 15
story = []

def p(text, style='BodyText'):
    story.extend([Paragraph(text, styles[style]), Spacer(1, 8)])

def code(text, numbered=False, first=1):
    lines = []
    for n, line in enumerate(text.splitlines(), first):
        line = f'{n:3}  {line}' if numbered else line
        lines.extend(textwrap.wrap(line, 106, expand_tabs=False, replace_whitespace=False, drop_whitespace=False) or [''])
    story.append(Preformatted('\n'.join(lines), styles['Codigo']))

def source(path, start=None, stop=None):
    data = (ROOT / path).read_text(encoding='utf-8').splitlines()
    code('\n'.join(data[start:stop]), numbered=True, first=(start or 0)+1)

base = 'app/src/main/java/com/app/changescout/'
p('ChangeScout | Timeout y conectividad', 'Title')
p('Evidencia de los puntos 2, 3 y 5', 'Heading2')
p('<b>Resultado: dos pruebas especializadas aprobadas, cero errores y cero fallos.</b>')
p('Registro JUnit: ' + escape(suite.attrib['timestamp']))
p('<b>2. Timeout configurable:</b> ConfiguracionRed recibe timeoutMillis por constructor. FabricaClienteHttp aplica el valor al limite total y a conexion, lectura y escritura. La inyeccion de dependencias configura 3.000 ms y el cliente del backend hereda ese mismo limite.')
p('<b>3. Errores semanticos:</b> SocketTimeoutException y el timeout total se transforman en Tiempo agotado; SocketException y UnknownHostException, en Sin conexión. El ViewModel real recibe Fallo, muestra el mensaje y establece estaCargando=false.')
p('<b>5. Pruebas:</b> un doble de API demora 4.000 ms y se cancela al alcanzar el limite de 3.000 ms. Otro lanza SocketException. Se verifica la transicion del spinner activo al error y una sola llamada, sin reintentos.')
p('Las pruebas usan tiempo virtual con kotlinx-coroutines-test; no esperan cuatro segundos reales ni necesitan sockets, Supabase, emulador o celular. Se prueban el repositorio y el ViewModel reales. Gradle usa --offline con las dependencias ya descargadas.')
p('Alcance práctico', 'Heading2')
p('El limite de tres segundos se aplica a la app Android para esta evaluacion. Puede ser insuficiente para tareas largas de marketplace/NLP; se puede ajustar en la configuracion inyectada. El backend sigue pendiente de despliegue.')
p('Repetir desde PowerShell', 'Heading2')
code('.\\scripts\\test-conectividad.ps1')
p('El script guarda el comando, toda la salida y el XML de JUnit en output/conectividad. El codigo fuente aparece reproducido literalmente en las paginas siguientes, con numeros de linea.')
if not (OUT / 'terminal-captura.jpg').exists():
    p('<b>Captura del terminal pendiente:</b> se adjunta la transcripcion integra. La herramienta no permite automatizar terminales graficas; falta capturar la ejecucion manual para completar esa parte visual de la consigna.')
p('Referencias tecnicas: <link href="https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/">Kotlin: pruebas con tiempo virtual</link>.')

story.append(PageBreak())
p('2 | Constructor y configuracion del cliente', 'Heading1')
p('ConfiguracionRed.kt - codigo fuente completo')
source(base+'data/api/ConfiguracionRed.kt')
p('ModulosChangeScout.kt - inyeccion y cliente del backend', 'Heading2')
mod = (ROOT / (base+'app/ModulosChangeScout.kt')).read_text(encoding='utf-8')
start = mod.index('    @Provides', mod.index('fun provideAppScope'))
stop = mod.index('    @Provides', mod.index('.build()', start))
code(mod[start:stop])

story.append(PageBreak())
p('3 | De excepciones a errores de la app', 'Heading1')
p('ErroresRed.kt - codigo fuente completo')
source(base+'data/api/ErroresRed.kt')
p('SesionSupabase.kt - limite de la operacion y captura', 'Heading2')
auth = (ROOT / (base+'data/auth/SesionSupabase.kt')).read_text(encoding='utf-8')
start = auth.index('            val response = withTimeout')
stop = auth.index('        } catch (error: RuntimeException)', start)
code(auth[start:stop])
p('ViewModelSesion.kt - transicion del progreso al error', 'Heading2')
vm = (ROOT / (base+'ui/viewmodel/ViewModelSesion.kt')).read_text(encoding='utf-8')
start = vm.index('                    is ResultadoOperacion.Fallo ->')
stop = vm.index('                    is ResultadoOperacion.DatosObsoletos', start)
code(vm[start:stop])

test = 'app/src/test/java/com/app/changescout/ui/viewmodel/ConectividadTest.kt'
lines = (ROOT/test).read_text(encoding='utf-8').splitlines()
for start in range(0, len(lines), 44):
    story.append(PageBreak())
    p('5 | Codigo de las dos pruebas', 'Heading1')
    p(f'ConectividadTest.kt - lineas {start+1} a {min(start+44, len(lines))}')
    source(test, start, start+44)

raw = (OUT/'terminal.txt').read_text(encoding='utf-16')
parts = raw.split('> Task :app:testDebugUnitTest\n', 1)
for i, part in enumerate(parts):
    story.append(PageBreak())
    p('5 | ' + ('Comando y salida completa' if i == 0 else 'Resultados y cierre del test runner'), 'Heading1')
    code(('> Task :app:testDebugUnitTest\n' if i else '') + part)
capture = OUT / 'terminal-captura.jpg'
if capture.exists():
    story.append(PageBreak())
    p('5 | Captura real del terminal', 'Heading1')
    pic = Image(str(capture))
    factor = min(511/pic.imageWidth, 690/pic.imageHeight)
    pic.drawWidth, pic.drawHeight = pic.imageWidth*factor, pic.imageHeight*factor
    story.append(pic)

def footer(c, doc):
    c.setFont('Helvetica', 8)
    c.setFillColor(colors.HexColor('#526575'))
    c.drawString(42, 24, 'ChangeScout - Puntos 2, 3 y 5')
    c.drawRightString(A4[0]-42, 24, str(doc.page))

pdf = ROOT/'output/pdf/evidencia-conectividad.pdf'
pdf.parent.mkdir(parents=True, exist_ok=True)
SimpleDocTemplate(str(pdf), pagesize=A4, leftMargin=42, rightMargin=42, topMargin=38, bottomMargin=40).build(story, onFirstPage=footer, onLaterPages=footer)
doc = fitz.open(pdf)
text = ''.join(p.get_text() for p in doc)
assert text.count(' PASSED') == 2 and 'BUILD SUCCESSFUL' in text
render = ROOT/'tmp/pdfs-conectividad'
render.mkdir(parents=True, exist_ok=True)
for i, page in enumerate(doc):
    page.get_pixmap(matrix=fitz.Matrix(1.2, 1.2)).save(str(render/f'pagina-{i+1}.png'))
print(f'{pdf}: {len(doc)} paginas; 2 PASSED y BUILD SUCCESSFUL verificados.')
