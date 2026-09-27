FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY . .

RUN useradd --create-home appuser
USER appuser
EXPOSE 5055
CMD ["gunicorn", "--bind", "0.0.0.0:5055", "--preload", "--workers", "2", "--threads", "2", "app:app"]
