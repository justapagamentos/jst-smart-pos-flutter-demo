
# Justa: Smart POS (Demo)

[![Flutter](https://img.shields.io/badge/Flutter-3.24.0-blue.svg)](https://flutter.dev)
[![License](https://img.shields.io/badge/License-Apache-green.svg)](https://opensource.org/licenses/Apache-2.0)

Este é um projeto **demo** desenvolvido em **Flutter** para realizar a integração com nosso terminal **Smart POS**. O aplicativo realiza operações de captura de vendas em **débito**, **crédito**, e **Pix** através de serviço nativo em Android para realizar as operações.

## Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Execução do Projeto](#execução-do-projeto)
- [Estrutura do Projeto Android](#estrutura-do-projeto-android)
- [Bibliotecas Utilizadas](#bibliotecas-utilizadas)
- [Contribuição](#contribuição)
- [Licença](#licença)
- [Sobre a Justa](#sobre-a-justa)

## Sobre o Projeto

Este projeto utiliza **Flutter** para criar uma interface de usuário cross-platform, com foco na execução em dispositivos Android. Ele foi desenvolvido para integrar-se com o terminal **Smart POS** da Justa, permitindo a realização de transações financeiras como pagamento via **débito**, **crédito**, e **Pix**.

### Requisitos

- **SDK Android 22 (Lollipop)** ou superior é obrigatório para rodar este aplicativo corretamente.

## Funcionalidades

- [x] Captura de transações de **débito**, **crédito**, e **Pix**.
- [x] Integração com o terminal **Smart POS**.
- [x] Suporte completo para Android.
- [x] Comunicação via serviço nativo Android.
- [x] Relatórios de sucesso e falhas nas transações via `EventChannel` e `MethodChannel`.

## Instalação

### Pré-requisitos

- **Flutter SDK** instalado na versão mais recente - e.g. `3.24.0`.
- **Android Studio** ou outra IDE com suporte a Flutter.
- **Dispositivo ou emulador Android** com SDK >= 22 (Lollipop).

### Passos

1. Clone o repositório:

   ```bash
   git clone https://github.com/seu-usuario/smartpos-demo.git
   ```

2. Navegue até o diretório do projeto:

   ```bash
   cd smartpos-demo
   ```

3. Execute o comando para instalar as dependências:

   ```bash
   flutter pub get
   ```

## Configuração

Certifique-se de que o dispositivo Android tem o **SDK 22 (Lollipop)** ou superior. O projeto utiliza APIs nativas do Android para comunicação com o terminal **Smart POS**.

## Execução do Projeto

1. Para rodar o aplicativo no Android, use o comando:

   ```bash
   flutter run
   ```

2. Certifique-se de ter um dispositivo Smart POS com **SDK Android >= 22**.

## Estrutura do Projeto Android

### Classes principais

- **`MainActivity.kt`**: A activity principal que serve de ponto de entrada para o Flutter e integração com o serviço Android.
- **`JustaApplication.kt`**: Classe responsável pela inicialização global da aplicação e configuração do terminal que deve ser configurada no `AndroidManifest.xml` dentro da tag `<application name>`.
- **`dto/`**: Contém classes de transferência de dados (`SdkPaymentResult`, `SdkTerminalInit`, `SdkTerminalData`, `SdkTerminalNotification`, `SdkError`) para comunicação entre o serviço Android e o Flutter.

## Bibliotecas Utilizadas

- **Flutter**: Framework para desenvolvimento de UI multiplataforma.
- **Android SDK**: Utilizado para integração com o sistema Android e comunicação com o terminal Smart POS.

## Contribuição

Aceitamos contribuições para melhorar este projeto.
Por favor, faça um fork do repositório e envie um pull request com suas mudanças.

## Licença

Este projeto está licenciado sob a **Licença Apache 2.0**.
Sinta-se à vontade para usar e modificar.

## Sobre a Justa

Este projeto foi desenvolvido pela [Justa](https://www.justa.com.vc), uma fintech que oferece soluções inovadoras de pagamento e serviços financeiros no Brasil.
