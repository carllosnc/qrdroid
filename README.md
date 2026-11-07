# QRDroid 📱⚡

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Platform-Android%2024%2B-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![ML Kit](https://img.shields.io/badge/Google%20ML%20Kit-Barcode%20Scanning-EA4335.svg?style=flat&logo=google&logoColor=white)](https://developers.google.com/ml-kit/vision/barcode-scanning)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**QRDroid** é um aplicativo Android moderno, rápido e intuitivo para leitura e geração de QR Codes e códigos de barras. Desenvolvido 100% em **Kotlin** com **Jetpack Compose** e **Material Design 3**, oferece suporte nativo especializado para o ecossistema brasileiro (PIX e Boletos), além de detecção ultrarrápida com Google ML Kit e CameraX.

---

## ✨ Funcionalidades Principais

### 🔍 Leitor Inteligente (Scanner)
- **Detecção Ultrarrápida:** Processamento em tempo real com **Google ML Kit Barcode Scanning**.
- **Controles de Câmera:** Suporte a zoom suave (0.5x, 1x, 2x, 5x), alternância de lanterna (flash) e troca entre câmera traseira/frontal.
- **Overlay Dinâmico:** Miras com animação de laser estilo scanner e cantos arredondados modernos.
- **Feedback Tátil:** Vibração e resposta tátil imediata ao reconhecer um código.

### 🇧🇷 Parsers Especializados
- **PIX (Padrão BACEN / EMV):** Identificação de nome do recebedor, cidade, chave PIX, identificador da transação e valor formatado (R$). Ação rápida para copiar código "Copia e Cola".
- **Boleto Bancário & Concessionárias:** Suporte a boletos de cobrança e contas de serviços (água, luz, gás, tributos), cálculo de data de vencimento, valor e formatação da Linha Digitável.
- **Wi-Fi:** Leitura instantânea de configurações de rede (SSID, senha, tipo de criptografia WPA/WEP/Open) e conexão direta.
- **Links & Navegação:** Pré-visualização de URLs com abertura segura no navegador padrão.
- **Contatos, E-mails e Telefones:** Ações contextuais de discagem, envio de mensagem e cópia.

### 🛠️ Gerador de QR Code
- Geração instantânea de QR Codes em alta resolução utilizando **ZXing**.
- Suporte a múltiplos tipos de conteúdo: Texto puro, URLs, Redes Wi-Fi, Chaves PIX, etc.
- Compartilhamento e exportação de imagem gerada via `FileProvider`.

### 📚 Histórico e Favoritos
- Armazenamento local seguro e offline via **SQLite**.
- Busca textual dinâmica e filtros por categoria (Todos, PIX, Boleto, URL, Wi-Fi).
- Marcação de itens favoritos, cópia de conteúdo e exclusão individual ou em lote.

---

## 🛠️ Tecnologias e Arquitetura

O projeto adota práticas recomendadas de engenharia de software Android moderna:

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material Design 3 (Material You)](https://m3.material.io/)
- **Tipografia:** Fonte Inter integrada com escala tipográfica Material 3.
- **Câmera:** [CameraX](https://developer.android.com/training/camerax) (Lifecycle, Camera2, View)
- **Visão Computacional:** [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **Geração de QR:** [ZXing Core](https://github.com/zxing/zxing)
- **Navegação:** [Jetpack Navigation 3](https://developer.android.com/guide/navigation)
- **Arquitetura:** MVVM (Model-View-ViewModel) com Coroutines e StateFlow
- **Banco de Dados:** SQLite nativo com padrão Repository

---

## 📂 Estrutura do Projeto

```text
qrdroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── kotlin/com/example/qrdroid/
│   │   │   │   ├── MainActivity.kt               # Ponto de entrada do app
│   │   │   │   ├── Navigation.kt                 # Grafo de navegação (Navigation 3)
│   │   │   │   ├── data/                         # Modelos, SQLite Helper e Repositório
│   │   │   │   ├── generator/                    # Gerador de QR Code com ZXing
│   │   │   │   ├── scanner/                      # Analisador ML Kit e Parsers (PIX, Boleto, Wi-Fi)
│   │   │   │   └── ui/                           # Telas Compose, ViewModels e Tema M3
│   │   │   │       ├── create/                   # Criação de QR Code
│   │   │   │       ├── history/                  # Histórico e favoritos
│   │   │   │       ├── main/                     # Scaffold principal e abas
│   │   │   │       ├── scanner/                  # Visor de câmera e BottomSheet de resultado
│   │   │   │       └── theme/                    # Cores, Tipografia Inter e Tema
│   │   │   └── res/                              # Ícones, fontes Inter e XMLs de configuração
│   │   └── test/                                 # Testes unitários de parsers e ViewModels
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                        # Version Catalog de dependências
└── README.md
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio Ladybug** (ou versão superior)
- **JDK 17** configurado
- Dispositivo físico Android (recomendado para testes de câmera) ou Emulador com suporte a câmera virtual (API 24+)

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/carllosnc/qrdroid.git
   cd qrdroid
   ```

2. Abra o projeto no **Android Studio**.

3. Sincronize as dependências do Gradle (**Sync Project with Gradle Files**).

4. Compile e execute em seu dispositivo/emulador:
   ```bash
   ./gradlew assembleDebug
   ```

5. Para rodar os testes unitários:
   ```bash
   ./gradlew test
   ```

---

## 📄 Licença

Este projeto está sob a licença [MIT](LICENSE).
