#!/bin/bash
echo "Waiting for SQL Server to start..."
sleep 30
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Password123!" -No -Q "CREATE DATABASE QLiDienThoai"
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Password123!" -No -d QLiDienThoai -i /init.sql
echo "Database initialized!"