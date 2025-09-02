import os
import subprocess
import google.generativeai as genai

# Configura a chave da API do Gemini
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

# Carrega o modelo do Gemini
model = genai.GenerativeModel("models/gemini-1.5-pro-latest")

def gerar_diff_com_novos_arquivos():
    base_ref = os.getenv("GITHUB_BASE_REF", "main")
    try:
        # Faz fetch da base da PR
        subprocess.run(["git", "fetch", "origin", base_ref], check=True)

        # Descobre o commit-base comum entre a base e o PR
        merge_base_proc = subprocess.run(
            ["git", "merge-base", f"origin/{base_ref}", "HEAD"],
            stdout=subprocess.PIPE,
            text=True,
            check=True
        )
        merge_base = merge_base_proc.stdout.strip()

        # Gera o diff a partir do merge-base
        diff_proc = subprocess.run(
            ["git", "diff", f"{merge_base}..HEAD"],
            stdout=subprocess.PIPE,
            text=True,
            check=True
        )
        diff = diff_proc.stdout

        # Pega os arquivos adicionados
        arquivos_proc = subprocess.run(
            ["git", "diff", "--name-status", f"{merge_base}..HEAD"],
            stdout=subprocess.PIPE,
            text=True,
            check=True
        )

        conteudo_novos = ""
        for linha in arquivos_proc.stdout.splitlines():
            status, path = linha.split('\t', 1)
            if status == "A" and (path.endswith(".java") or path.endswith(".sql")):
                try:
                    with open(path, encoding="utf-8") as f:
                        conteudo_novos += f"\n\n### Arquivo novo: {path}\n" + f.read()
                except Exception as e:
                    print(f"Erro ao ler {path}: {e}")

        return diff + conteudo_novos

    except subprocess.CalledProcessError as e:
        print(f"Erro ao gerar diff: {e.stderr}")
        return ""


diff = gerar_diff_com_novos_arquivos()

prompt = f"""
Você é um avaliador técnico da Alura que recebeu um teste de um candidato. Avalie as mudanças no projeto Java com Spring Boot com base no seguinte diff de código:

{diff}

---

### Questão 1.1: Resposta Aberta (`/task/new/opentext`)
- O endpoint foi implementado para receber o json de referencia?
```json
 {{
    \"courseId\": 42,
    \"statement\": \"O que aprendemos na aula de hoje?\",
    \"order\": 1
 }}
 ```
- Foi respeitada a regra de que só cursos com status `BUILDING` podem receber atividades?
- Existe validação para impedir enunciados duplicados no mesmo curso?
- A ordem da atividade é positiva e contínua?
- Existem testes unitários para o endpoint? cobrem a maioria dos casos?

### Questão 1.2: Alternativa Única (`/task/new/singlechoice`)
- O endpoint foi implementado para receber o json de referencia?
```json
 {{
         \"courseId\": 42,
         \"statement\": \"O que aprendemos hoje?\",
         \"order\": 2,
         \"options\": [
             {{
                 \"option\": \"Java\",
                 \"isCorrect\": true
             }},
             {{
                 \"option\": \"Python\",
                 \"isCorrect\": false
             }},
             {{
                 \"option\": \"Ruby\",
                 \"isCorrect\": false
             }}
         ]
 }}
 ```
- Foi respeitada a regra de que só cursos com status `BUILDING` podem receber atividades?
- Existe validação para impedir enunciados duplicados no mesmo curso?
- A ordem da atividade é positiva e contínua?
- Existem entre 2 e 5 alternativas?
- Existe validação para existir somente uma alternativa é correta?
- Alternativas seguem os requisitos (4 a 80 caracteres, únicas entre si e diferentes do enunciado)?
- Existem testes unitários para o endpoint? cobrem a maioria dos casos?

### Questão 1.3: Alternativa de multipla escolha (`/task/new/multiplechoice`)

- O endpoint foi implementado para receber o json de referencia?
```json
 {{
         \"courseId\": 42,
         \"statement\": \"O que aprendemos hoje?\",
         \"order\": 2,
         \"options\": [
             {{
                 \"option\": \"Java\",
                 \"isCorrect\": true
             }},
             {{
                 \"option\": \"Python\",
                 \"isCorrect\": false
             }},
             {{
                 \"option\": \"Ruby\",
                 \"isCorrect\": false
             }}
         ]
 }}
 ```
- Foi respeitada a regra de que só cursos com status `BUILDING` podem receber atividades?
- Existe validação para impedir enunciados duplicados no mesmo curso?
- A ordem da atividade é positiva e contínua?
- Existem entre 3 e 5 alternativas?
- Segue a regra de pelo ao menos 2 corretas e 1 incorreta?
- Alternativas seguem os requisitos (4 a 80 caracteres, únicas entre si e diferentes do enunciado)?
- Existem testes unitários para o endpoint? cobrem a maioria dos casos?

### Questão 2: Publicação de Curso
- Existe endpoint POST `/course/{{id}}/publish`?
- O curso só pode ser publicado se estiver com status `BUILDING`?
- O curso só pode ser publicado as atividades estiverem com ordens contínuas?
- Após publicação, o status é alterado para `PUBLISHED` e `publishedAt` é preenchido corretamente?

### Modelegem de atividades (Task)
- Avalie a modelagem de atividades (task) do candidato, existe uma classe abstrata `Task` e as classes filhas `OPEN_TEXT`, `SINGLE_CHOICE` e `MULTIPLE_CHOICE`? faça um breve resumo de pontos positivos e negativos.


### Questão Bônus (se implementado)
- O Spring Security foi configurado?
- Apenas usuários com role `INSTRUCTOR` conseguem acessar endpoints de criação e publicação?
- Endpoints públicos estão acessíveis a usuários autenticados?
- O fluxo de autenticação esta funcional? como o login é feito?

---
### Resultado Esperado

Gere um **parecer técnico completo**, contendo:
- ✅ Pontos positivos
- ⚠️ Oportunidades de melhoria
- 🔁 Sugestões de refatoração
- 🧪 Qualidade dos testes
- 📊 Nota final de 0 a 10
"""

# Envia o prompt pro Gemini
response = model.generate_content(prompt)

# Escreve o resultado num arquivo markdown
with open("relatorio.md", "w", encoding="utf-8") as f:
    f.write("# ✅ Relatório de Avaliação Automática\n\n")
    f.write(response.text)
