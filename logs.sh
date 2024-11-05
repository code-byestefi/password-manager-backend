#!/bin/bash

# mostrar los logs
show_logs() {
    service=$1
    lines=$2

    case $service in
        "app")
            docker logs password-manager-app --tail $lines -f
            ;;
        "db")
            docker logs password-manager-db --tail $lines -f
            ;;
        "all")
            docker-compose -f docker-compose.prod.yml logs --tail $lines -f
            ;;
        *)
            echo "Servicio no válido. Use: app, db, o all"
            exit 1
            ;;
    esac
}

# Verificar argumentos
SERVICE=${1:-"all"}  # default a "all" si no se especifica
LINES=${2:-100}      # default a 100 líneas si no se especifica

show_logs $SERVICE $LINES