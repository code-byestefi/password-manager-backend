#!/bin/bash

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backups"

# directorio de backups
mkdir -p $BACKUP_DIR

# Función para realizar backup de la base de datos
backup_database() {
    echo "📦 Realizando backup de la base de datos..."
    docker exec password-manager-db pg_dump -U postgres password_manager > "$BACKUP_DIR/backup_$TIMESTAMP.sql"
}

# Función para construir y desplegar
deploy() {
    echo "🚀 Iniciando despliegue..."

    # Detener contenedores existentes
    echo "⏳ Deteniendo servicios actuales..."
    docker-compose -f docker-compose.prod.yml down

    # Construir nuevas imágenes
    echo "🔨 Construyendo imágenes..."
    docker-compose -f docker-compose.prod.yml build

    # Iniciar servicios
    echo "▶️ Iniciando servicios..."
    docker-compose -f docker-compose.prod.yml up -d

    echo "✅ Despliegue completado!"
}

# Realizar backup si los contenedores están corriendo
if docker ps | grep -q password-manager-db; then
    backup_database
else
    echo "⚠️ Base de datos no está corriendo, saltando backup..."
fi

# Ejecutar despliegue
deploy

# Verificar estado
echo "🔍 Verificando estado de los servicios..."
docker-compose -f docker-compose.prod.yml ps