# Rede de Hopfield

Implementacao em Java de uma rede neural de Hopfield para reconhecer desenhos representados por valores `1` e `-1`.

## Requisitos

- Java JDK instalado, com os comandos `javac` e `java` disponiveis no terminal.
- Nenhuma biblioteca externa.

## Como executar

Abra o terminal na pasta do projeto e execute:

```powershell
javac RedeHopfield.java
java RedeHopfield
```

O programa le o arquivo `dados.csv` da pasta atual. Por isso, execute os comandos a partir da raiz do projeto ou mantenha o CSV na pasta em que o programa for iniciado.

Para limpar o arquivo compilado no PowerShell:

```powershell
Remove-Item RedeHopfield.class
```

No Linux ou macOS, use `rm RedeHopfield.class`.

## Formato do CSV

- Cada linha representa um desenho achatado.
- Os valores devem ser `1` ou `-1`; o programa tambem aceita valores decimais como `1,00` e `-1,00`.
- A primeira linha pode ser um cabecalho.
- As linhas anteriores a ultima sao os padroes armazenados.
- A ultima linha e o padrao desconhecido que sera reconhecido.
- O desenho atual possui `5 x 6` posicoes, totalizando 30 valores por linha.
- O separador pode ser `;` ou `,`.

## Estrutura

- `RedeHopfield.java`: codigo-fonte e ponto de entrada (`main`).
- `dados.csv`: padroes usados no treinamento e entrada a reconhecer.
- `RedeHopfield.iml`: arquivo de modulo do IntelliJ IDEA; guarda configuracoes basicas do modulo Java e nao e usado pelo `javac`.

## IntelliJ IDEA

Abra a pasta do projeto no IntelliJ IDEA e execute o metodo `main` da classe `RedeHopfield`. O arquivo `.iml` e reconhecido automaticamente como configuracao do modulo. Tambem e possivel executar pelo terminal usando os comandos acima.

## Git

Arquivos compilados (`.class`) e a pasta de saida do IntelliJ nao devem ser versionados. O `.gitignore` deste projeto ja exclui esses artefatos.