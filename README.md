# FIAP X - Sistema de Processamento de Vídeos

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring%20boot-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgresql-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/apache%20kafka-%23231F20.svg?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Funcionalidades](#funcionalidades)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Como Usar](#como-usar)
- [API Endpoints](#api-endpoints)
- [Arquitetura de Microserviços](#arquitetura-de-microserviços)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Testes](#testes)
- [Deploy](#deploy)
- [Contribuição](#contribuição)

## 🎯 Sobre o Projeto

O **FIAP X** é um sistema moderno de processamento de vídeos desenvolvido em **Kotlin** com **Spring Boot**. O sistema permite aos usuários fazer upload de vídeos, processar automaticamente extraindo frames, compactar os frames em arquivos ZIP e notificar por email quando o processamento estiver concluído.

### Principais Características

- **Processamento Assíncrono**: Utiliza Apache Kafka para processamento distribuído
- **Autenticação JWT**: Sistema seguro de autenticação baseado em tokens
- **Extração de Frames**: Utiliza FFmpeg para extrair frames dos vídeos
- **Notificação por Email**: Sistema automático de notificações
- **API RESTful**: Endpoints bem documentados com Swagger
- **Containerização**: Docker e Kubernetes ready
- **Arquitetura Clean**: Seguindo princípios de Clean Architecture

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture** com as seguintes camadas:

```
┌─────────────────────────────────────────────┐
│                Controllers                   │  ← Interface de entrada (REST API)
├─────────────────────────────────────────────┤
│                 Use Cases                   │  ← Regras de negócio da aplicação  
├─────────────────────────────────────────────┤
│                 Gateways                    │  ← Interface com sistemas externos
├─────────────────────────────────────────────┤
│               Data Sources                  │  ← Acesso a dados (DB, Kafka, etc)
└─────────────────────────────────────────────┘
```

### Fluxo de Processamento

1. **Upload**: Usuário faz upload do vídeo via API REST
2. **Armazenamento**: Vídeo é salvo no sistema de arquivos
3. **Evento**: Evento Kafka é publicado para processamento assíncrono
4. **Processamento**: Worker consome evento e processa o vídeo com FFmpeg
5. **Notificação**: Email é enviado ao usuário com o resultado

## 🚀 Tecnologias Utilizadas

### Backend
- **Kotlin 1.9.25** - Linguagem de programação principal
- **Spring Boot 3.5.4** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **JWT (JsonWebToken)** - Autenticação stateless

### Banco de Dados
- **PostgreSQL** - Banco de dados relacional principal
- **Flyway** - Migração de banco de dados

### Mensageria
- **Apache Kafka** - Sistema de mensageria distribuída
- **Spring Kafka** - Integração com Kafka

### Processamento de Vídeo
- **FFmpeg** - Extração de frames de vídeo
- **Java NIO** - Manipulação de arquivos

### Notificações
- **Spring Mail** - Envio de emails
- **SMTP Gmail** - Servidor de email

### Documentação
- **SpringDoc OpenAPI** - Documentação automática da API
- **Swagger UI** - Interface interativa da documentação

### DevOps
- **Docker** - Containerização
- **Docker Compose** - Orquestração local
- **Kubernetes** - Orquestração em produção

## 🎬 Funcionalidades

### 📤 Upload de Vídeos
- Upload de arquivos de vídeo (MP4, AVI, MOV, MKV, WMV, FLV, WebM)
- Limite de 50MB por arquivo
- Validação de formato e integridade

### 🎭 Processamento de Vídeos
- Extração automática de frames (1 FPS)
- Compactação em arquivo ZIP
- Processamento assíncrono com Kafka
- Retry automático em caso de falha

### 📧 Sistema de Notificações
- Email de confirmação de upload
- Email de sucesso no processamento
- Email de erro com detalhes do problema
- Templates HTML personalizados

### 🔐 Autenticação e Segurança
- Autenticação JWT stateless
- CORS configurado para todas as origens
- Informações do usuário extraídas do token
- Sem consultas desnecessárias ao banco

### 📊 Monitoramento
- Logs estruturados
- Health checks
- Métricas de performance
- Sistema de DLQ (Dead Letter Queue)

## ⚡ Pré-requisitos

### Software Necessário
- **Java 21** ou superior
- **FFmpeg** instalado no sistema
- **Docker** e **Docker Compose**
- **PostgreSQL** (se não usar Docker)
- **Apache Kafka** (se não usar Docker)

### Variáveis de Ambiente
```bash
# Banco de dados
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE=hackaton_db
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Email
EMAIL_USERNAME=seu-email@gmail.com
EMAIL_PASSWORD=sua-senha-app

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Servidor
SERVER_PORT=8080
```

## 🛠️ Instalação e Configuração

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd projeto-fiapx/hackaton
```

### 2. Configure as variáveis de ambiente
```bash
cp .env.example .env
# Edite o arquivo .env com suas configurações
```

### 3. Inicie a infraestrutura com Docker
```bash
docker-compose up -d postgres kafka
```

### 4. Execute a aplicação
```bash
# Usando Gradle Wrapper
./gradlew bootRun

# Ou compile e execute o JAR
./gradlew build
java -jar build/libs/fiapx-0.0.1-SNAPSHOT.jar
```

### 5. Acesse a aplicação
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 📖 Como Usar

### 1. Autenticação
Primeiro, você precisa de um token JWT válido. O token deve conter:
```json
{
  "email": "usuario@exemplo.com",
  "name": "Nome do Usuário", 
  "userId": "uuid-do-usuario",
  "sub": "usuario@exemplo.com",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### 2. Upload de Vídeo
```bash
curl -X POST "http://localhost:8080/api/upload" \
  -H "Authorization: Bearer SEU_JWT_TOKEN" \
  -F "video=@caminho/para/seu/video.mp4"
```

### 3. Verificar Status
```bash
curl -X GET "http://localhost:8080/api/status" \
  -H "Authorization: Bearer SEU_JWT_TOKEN"
```

### 4. Download do Resultado
```bash
curl -X GET "http://localhost:8080/api/download/frames_timestamp.zip" \
  -H "Authorization: Bearer SEU_JWT_TOKEN" \
  --output frames.zip
```

## 🔌 API Endpoints

### Upload de Vídeo
```http
POST /api/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>

Parâmetros:
- video: arquivo de vídeo (multipart/form-data)

Resposta:
{
  "id": "uuid",
  "status": "UPLOADED",
  "message": "Video uploaded successfully and queued for processing"
}
```

### Listar Vídeos do Usuário
```http
GET /api/status
Authorization: Bearer <token>

Resposta:
[
  {
    "id": "uuid",
    "userId": "uuid", 
    "originalVideoPath": "uploads/video.mp4",
    "zipPath": "frames_timestamp.zip",
    "frameCount": 120,
    "fileSize": 20016408,
    "status": "FINISHED",
    "uploadedAt": "2025-01-01T12:00:00Z",
    "message": null
  }
]
```

### Download de Arquivo
```http
GET /api/download/{filename}
Authorization: Bearer <token>

Resposta: Arquivo binário (application/octet-stream)
```

### Status dos Vídeos
- **UPLOADED**: Vídeo foi enviado e está na fila
- **PROCESSING**: Vídeo está sendo processado
- **FINISHED**: Processamento concluído com sucesso
- **ERROR**: Erro durante o processamento

## 🏢 Arquitetura de Microserviços

### Componentes Principais

#### 1. API Gateway (Controller Layer)
- **VideoController**: Gerencia uploads, status e downloads
- **Autenticação JWT**: Extração de dados do usuário do token
- **Validação**: Verificação de formatos e tamanhos

#### 2. Business Logic (Use Cases)
- **UploadVideoUseCase**: Processa upload e publica eventos
- **ProcessVideoUseCase**: Extrai frames e cria ZIP
- **ListVideoUseCase**: Lista vídeos do usuário
- **SendEmailUseCase**: Envia notificações

#### 3. Data Access (Gateways & Data Sources)
- **VideoGateway**: Interface com banco de dados
- **VideoEventGateway**: Interface com Kafka
- **EmailGateway**: Interface com SMTP

#### 4. Event Processing
- **VideoEventConsumer**: Consome eventos do Kafka
- **DlqConsumer**: Processa mensagens com erro
- **Retry Logic**: Sistema de reprocessamento

### Padrões Utilizados

- **CQRS**: Separação de comandos e consultas
- **Event Sourcing**: Eventos para processamento assíncrono
- **Repository Pattern**: Abstração do acesso a dados
- **Dependency Injection**: Inversão de dependências
- **Circuit Breaker**: Proteção contra falhas

## 📁 Estrutura do Projeto

```
src/main/kotlin/hackaton/fiapx/
├── adapters/
│   ├── controllers/        # REST Controllers
│   ├── gateways/          # Implementações de interface externa
│   ├── presenters/        # Mappers de resposta
│   └── services/          # Serviços de infraestrutura
├── commons/
│   ├── config/            # Configurações (Security, Kafka, etc)
│   ├── dto/               # Data Transfer Objects
│   ├── enums/             # Enumerações
│   ├── exception/         # Exceções customizadas
│   └── interfaces/        # Contratos e interfaces
├── entities/              # Entidades de domínio
└── usecases/              # Regras de negócio
    ├── auth/              # Casos de uso de autenticação
    ├── process/           # Casos de uso de processamento
    └── user/              # Casos de uso de usuário

src/main/resources/
├── application.yml        # Configurações da aplicação
├── application-local.yml  # Configurações locais
└── templates/             # Templates de email

src/test/                  # Testes unitários e integração
```

## 🧪 Testes

### Executar Todos os Testes
```bash
./gradlew test
```

### Executar Testes Específicos
```bash
# Testes de uma classe específica
./gradlew test --tests VideoControllerTest

# Testes de um pacote
./gradlew test --tests "hackaton.fiapx.usecases.*"
```

### Coverage Report
```bash
./gradlew jacocoTestReport
# Relatório em: build/reports/jacoco/test/html/index.html
```

### Tipos de Teste
- **Testes Unitários**: Testam componentes isoladamente
- **Testes de Integração**: Testam integração entre componentes
- **Testes de API**: Testam endpoints REST
- **Testes de Kafka**: Testam produção e consumo de eventos

## 🚢 Deploy

### Docker Build
```bash
# Build da imagem
docker build -t fiapx:latest .

# Run do container
docker run -p 8080:8080 \
  -e DATABASE_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  fiapx:latest
```

### Docker Compose
```bash
# Subir toda a stack
docker-compose up -d

# Logs da aplicação
docker-compose logs -f app

# Parar todos os serviços
docker-compose down
```

### Kubernetes
```bash
# Deploy no cluster
kubectl apply -f k8s/

# Verificar pods
kubectl get pods

# Logs da aplicação
kubectl logs -f deployment/hackaton-app
```

### Variáveis de Ambiente para Produção
```yaml
# k8s/config.yaml
DATABASE_HOST: postgres-service
DATABASE_PORT: "5432"
KAFKA_BOOTSTRAP_SERVERS: kafka-service:9092
EMAIL_USERNAME: your-production-email@company.com
SERVER_PORT: "8080"
```

## 🔧 Configurações Avançadas

### Kafka Topics
```bash
# Criar tópicos necessários
kafka-topics --create --topic video-processing --partitions 3 --replication-factor 1
kafka-topics --create --topic video-dlq --partitions 1 --replication-factor 1
```

### FFmpeg Installation
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install ffmpeg

# macOS
brew install ffmpeg

# Windows
# Baixar de https://ffmpeg.org/download.html
```

### Email Configuration (Gmail)
1. Ative a autenticação de 2 fatores
2. Gere uma senha de aplicativo
3. Use a senha de aplicativo na variável `EMAIL_PASSWORD`

## 📊 Monitoramento e Logs

### Estrutura de Logs
```json
{
  "timestamp": "2025-01-01T12:00:00.000Z",
  "level": "INFO",
  "logger": "hackaton.fiapx.adapters.controllers.VideoController",
  "message": "Video upload request received",
  "userId": "uuid",
  "videoId": "uuid",
  "fileName": "video.mp4"
}
```

### Health Checks
- **Database**: Verifica conexão com PostgreSQL
- **Kafka**: Verifica conectividade com brokers
- **Disk Space**: Monitora espaço em disco
- **Memory**: Monitora uso de memória

### Métricas Disponíveis
- Número de vídeos processados
- Tempo médio de processamento
- Taxa de erro por tipo
- Uso de recursos (CPU, memória, disco)

## 🤝 Contribuição

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Add nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

### Padrões de Código
- Use **Kotlin Coding Conventions**
- Escreva testes para novas funcionalidades
- Mantenha cobertura de testes acima de 80%
- Documente APIs com OpenAPI/Swagger

### Issues e Bugs
- Use os templates de issue disponíveis
- Inclua logs relevantes
- Descreva passos para reproduzir
- Mencione versão e ambiente

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Equipe

- **Desenvolvedor Principal**: [Seu Nome]
- **Arquiteto de Soluções**: [Nome]
- **DevOps Engineer**: [Nome]

## 📞 Suporte

- **Email**: suporte@fiapx.com
- **Documentação**: [Link para docs]
- **Issues**: [Link para GitHub Issues]
- **Wiki**: [Link para Wiki]

---

⭐ **Se este projeto foi útil para você, não esqueça de dar uma estrela!**

---

*Desenvolvido com ❤️ pela equipe FIAP X*
