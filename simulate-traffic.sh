#!/bin/bash

BASE_URL="http://localhost:8081/api/transactions"

echo "--------------------------------------------------"
echo "🚀 INICIANDO SIMULAÇÃO DE TRÁFEGO - SENTINEL"
echo "--------------------------------------------------"

# 1. Transação Normal (Fluxo Feliz)
echo "📦 Enviando transação normal para USER-001..."
curl -s -X POST $BASE_URL \
-H "Content-Type: application/json" \
-d '{"accountId": "USER-001", "amount": 150.00, "merchant": "Apple Store"}' | jq .
echo -e "\n✅ Transação enviada.\n"

sleep 1

# 2. Simulação de Fraude (Volume Excessivo)
echo "🚨 Iniciando rajada de transações para ACC-FRAUD-01 (Limite: 3 em 2min)..."
for i in {1..4}
do
   echo "🔥 Enviando transação $i/4..."
   curl -s -X POST $BASE_URL \
   -H "Content-Type: application/json" \
   -d "{\"accountId\": \"ACC-FRAUD-01\", \"amount\": $((i * 10)).0, \"merchant\": \"Loja Express $i\"}" | jq .
   echo -e ""
   sleep 0.5
done

echo "--------------------------------------------------"
echo "🏁 SIMULAÇÃO CONCLUÍDA!"
echo "Verifique os tópicos no AKHQ (http://localhost:8080):"
echo "- transactions-raw (Histórico completo)"
echo "- transactions-validated (Transações normais)"
echo "- transactions-alerts (Transações suspeitas)"
echo "--------------------------------------------------"
