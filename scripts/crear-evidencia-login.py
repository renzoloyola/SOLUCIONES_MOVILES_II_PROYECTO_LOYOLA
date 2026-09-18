"""Genera el PDF desde la salida real y el XML de JUnit, sin inventar resultados."""
from pathlib import Path
import sys, textwrap, xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'tmp/pdf-deps'))
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Preformatted, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
import pymupdf as fitz

out = ROOT / 'output/pdf'
out.mkdir(parents=True, exist_ok=True)
raw = (ROOT / 'output/login/terminal.txt').read_text(encoding='utf-16')
suite = ET.parse(ROOT / 'output/login/resultados-login.xml').getroot()
assert suite.attrib['tests'] == '3' and suite.attrib['failures'] == '0' and suite.attrib['errors'] == '0'
styles = getSampleStyleSheet()
styles.add(ParagraphStyle('CodeSmall', fontName='Courier', fontSize=8, leading=10))
styles['Title'].textColor = colors.HexColor('#153C56')
styles['BodyText'].leading = 15
story = []
def p(text, style='BodyText'):
    story.append(Paragraph(text, styles[style]))
    story.append(Spacer(1, 10))
p('ChangeScout | Pruebas unitarias del login', 'Title')
p('Punto 4 - Evidencia de ejecución por consola', 'Heading2')
p('Resultado: <b>3 pruebas aprobadas, 0 fallos, 0 errores.</b><br/>Fecha del reporte JUnit: ' + escape(suite.attrib.get('timestamp', '')))
p('Se ejecutó el repositorio real <b>RepositorioSesionSupabase</b> en la JVM, con JUnit 4. La API se sustituyó por un doble local y la persistencia por un almacén en memoria. No se utilizó emulador, dispositivo ni red para ejecutar las pruebas. Gradle se ejecutó con <b>--offline</b>, usando dependencias previamente disponibles.')
p('Condiciones verificadas', 'Heading2')
p('<b>(a) Estado inicial:</b> al instanciar el servicio no existe sesión ni token. Restaurar una sesión vacía devuelve null y no llama a la API.')
p('<b>(b) Autenticación exitosa:</b> las credenciales esperadas producen Exito; el repositorio expone la sesión activa y persiste exactamente los tokens de acceso y renovación recibidos. Se verifica una escritura y una llamada.')
p('<b>(c) Autenticación rechazada:</b> la API simulada devuelve HTTP 400 con invalid_credentials. El resultado es Fallo y el mensaje de credenciales incorrectas. No se guarda sesión ni token y el contador permanece en una llamada: cero reintentos.')
p('Alcance: estas pruebas verifican el estado del repositorio y su contrato de persistencia. No verifican la pantalla Android ni el cifrado real de Android Keystore.')
p('Reproducción', 'Heading2')
p('Desde la raíz del proyecto, en PowerShell:')
story.append(Preformatted('.\\scripts\\test-login.ps1', styles['CodeSmall']))
p('El script configura Java, utiliza valores ficticios de Supabase y guarda la salida completa en output/login/terminal.txt y el reporte JUnit en output/login/resultados-login.xml. --rerun-tasks obliga a ejecutar las pruebas otra vez.')
p('Evidencia adjunta', 'Heading2')
p('Las siguientes páginas contienen el comando exacto y la transcripción completa, sin omitir advertencias ni tareas. Solo se ajustaron los saltos de línea al ancho de la página. <b>No es una captura de pantalla.</b> La captura visual del terminal queda pendiente: la herramienta de captura bloqueó la automatización de PowerShell.')
story.append(PageBreak())
p('Comando y salida completa del test runner', 'Heading1')
parts = raw.split('> Task :app:testDebugUnitTest\n', 1)
assert len(parts) == 2
for index, part in enumerate(parts):
    if index:
        story.append(PageBreak())
        p('Salida completa: resultados y cierre', 'Heading1')
        part = '> Task :app:testDebugUnitTest\n' + part
    lines = []
    for line in part.splitlines():
        lines.extend(textwrap.wrap(line, width=100, expand_tabs=False, replace_whitespace=False, drop_whitespace=False) or [''])
    story.append(Preformatted('\n'.join(lines), styles['CodeSmall']))
def footer(c, doc):
    c.setFont('Helvetica', 8)
    c.setFillColor(colors.HexColor('#526575'))
    c.drawString(42, 24, 'ChangeScout - Evidencia de pruebas locales')
    c.drawRightString(A4[0]-42, 24, str(doc.page))
pdf = out / 'evidencia-login.pdf'
SimpleDocTemplate(str(pdf), pagesize=A4, rightMargin=42, leftMargin=42, topMargin=38, bottomMargin=40).build(story, onFirstPage=footer, onLaterPages=footer)
doc = fitz.open(pdf)
render = ROOT / 'tmp/pdfs'
render.mkdir(parents=True, exist_ok=True)
for n, page in enumerate(doc):
    page.get_pixmap(matrix=fitz.Matrix(1.3, 1.3)).save(str(render / f'pagina-{n+1}.png'))
text = ''.join(page.get_text() for page in doc)
assert text.count(' PASSED') == 3 and 'BUILD SUCCESSFUL' in text
print(f'{pdf}: {len(doc)} paginas verificadas; 3 PASSED y BUILD SUCCESSFUL presentes.')
