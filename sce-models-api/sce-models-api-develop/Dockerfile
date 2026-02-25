# Use an official Python runtime as a parent image
FROM python:3.11-slim-bullseye

ENV TZ=America/Lima
# Set the working directory in the container
WORKDIR /app

# Add current directory code to working directory
COPY requirements.txt .

# Install system dependencies
RUN apt-get update && \
  apt-get install -y --no-install-recommends \
  poppler-utils \
  ffmpeg \
  libsm6 \
  libxext6 \
  && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
RUN pip install --no-cache-dir pip==25.1.0 && \
    pip install --no-cache-dir wheel==0.45.1 && \
    pip install --no-cache-dir -r requirements.txt

# Copy code
ADD . /app

COPY entrypoint.sh /app/entrypoint.sh

# Dar permisos de ejecución
RUN chmod +x /app/entrypoint.sh

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1
ENV FLASK_APP=app.py
ENV FLASK_ENV=production
ENV FLASK_RUN_HOST=0.0.0.0
# ENV FLASK_APPLICATION_ROOT=/

# Expose port
EXPOSE 80

# Start the application
CMD ["/app/entrypoint.sh"]
