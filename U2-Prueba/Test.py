from locust import HttpUser, task, between
import random
import string
from datetime import date, timedelta
import time
import threading

class PublicacionesUser(HttpUser):
    host = "http://localhost:8000/api"
    wait_time = between(0.5, 1.5)

    autor_ids = []
    autor_ids_lock = threading.Lock()
    libro_ids = []
    paper_ids = []
    autores_con_publicaciones = set()

    def random_string(self, length=8):
        return ''.join(random.choices(string.ascii_letters, k=length))

    def random_email(self):
        return f"user{int(time.time()*1000)}{random.randint(1000,9999)}@test.com"

    def random_orcid(self):
        return f"{random.randint(1000,9999)}-{random.randint(1000,9999)}-{random.randint(1000,9999)}-{random.randint(1000,9999)}-{random.randint(1000,9999)}"

    def random_isbn(self):
        return f"{int(time.time()*1000)}{random.randint(100,999)}"

    @task(3)
    def crear_autor(self):
        payload = {
            "nombre": f"Nombre{self.random_string(4)}",
            "apellido": f"Apellido{self.random_string(4)}",
            "email": self.random_email(),
            "nacionalidad": random.choice(["Argentina", "Chile", "Perú", "España"]),
            "institucion": random.choice(["Universidad A", "Instituto B", "Centro C"]),
            "orcid": self.random_orcid()
        }
        with self.client.post("/autores", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    json_data = response.json()
                    created = json_data.get("dato")
                    autor_id = created.get("id")
                    if autor_id:
                        with self.autor_ids_lock:
                            self.autor_ids.append(autor_id)
                    response.success()
                except Exception as e:
                    response.failure(f"Error parsing response: {e}. Raw text: {response.text}")
            else:
                response.failure(f"Failed to create author: {response.text}")

    @task(2)
    def listar_autores(self):
        self.client.get("/autores")

    @task(2)
    def consultar_autor_por_id(self):
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
        self.client.get(f"/autores/{autor_id}")

    @task(1)
    def actualizar_autor(self):
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
        payload = {
            "nombre": f"NombreActualizado{self.random_string(3)}",
            "apellido": f"ApellidoActualizado{self.random_string(3)}",
            "email": self.random_email(),
            "nacionalidad": "ActualizadoLandia",
            "institucion": "Instituto Actualizado",
            "orcid": self.random_orcid()
        }
        self.client.put(f"/autores/{autor_id}", json=payload)

    @task(1)
    def eliminar_autor(self):
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            # Buscar un autor sin publicaciones
            for idx, autor_id in enumerate(self.autor_ids):
                if autor_id not in self.autores_con_publicaciones:
                    removed_id = self.autor_ids.pop(idx)
                    break
            else:
                return

        with self.client.delete(f"/autores/{removed_id}", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed to delete author: {response.text}")

    @task(3)
    def crear_libro(self):
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
            self.autores_con_publicaciones.add(autor_id)

        payload = {
            "titulo": f"Libro {self.random_string(5)}",
            "anioPublicacion": random.randint(2000, 2025),
            "editorial": f"Editorial {self.random_string(4)}",
            "isbn": f"{random.randint(1000000000000,9999999999999)}",
            "resumen": "Resumen de prueba generado por Locust",
            "genero": random.choice(["Tecnología", "Fantasía", "Historia"]),
            "numeroPaginas": random.randint(100, 500),
            "autorId": autor_id
        }
        with self.client.post("/libros", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    created = response.json().get("dato")
                    libro_id = created.get("id")
                    if libro_id:
                        self.libro_ids.append(libro_id)
                    response.success()
                except Exception as e:
                    response.failure(f"Error parsing libro: {e}")
            else:
                response.failure(f"Error creando libro: {response.text}")

    @task(2)
    def listar_libros(self):
        self.client.get("/libros")

    @task(1)
    def obtener_libro_por_id(self):
        if not self.libro_ids:
            return
        libro_id = random.choice(self.libro_ids)
        self.client.get(f"/libros/{libro_id}")

    @task(1)
    def actualizar_libro(self):
        if not self.libro_ids:
            return
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
        libro_id = random.choice(self.libro_ids)
        payload = {
            "titulo": f"LibroActualizado {self.random_string(5)}",
            "anioPublicacion": random.randint(2000, 2025),
            "editorial": f"EditorialActualizada {self.random_string(4)}",
            "isbn": self.random_isbn(),
            "resumen": "Resumen actualizado generado por Locust",
            "genero": random.choice(["Tecnología", "Fantasía", "Historia"]),
            "numeroPaginas": random.randint(100, 500),
            "autorId": autor_id
        }
        self.client.put(f"/libros/{libro_id}", json=payload)

    @task(1)
    def eliminar_libro(self):
        if not self.libro_ids:
            return
        libro_id = self.libro_ids.pop()
        self.client.delete(f"/libros/{libro_id}")

    @task(3)
    def crear_paper(self):
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
            self.autores_con_publicaciones.add(autor_id)

        fecha_pub = date.today() - timedelta(days=random.randint(0, 3650))
        payload = {
            "titulo": f"Paper {self.random_string(5)}",
            "anioPublicacion": random.randint(2000, 2025),
            "editorial": f"EditorialPaper {self.random_string(4)}",
            "isbn": f"{random.randint(1000000000000,9999999999999)}",
            "resumen": "Resumen de prueba generado por Locust",
            "orcid": self.random_orcid(),
            "fechaPublicacion": fecha_pub.isoformat(),
            "revista": random.choice(["Nature", "Science", "IEEE", "ACM"]),
            "areaInvestigacion": random.choice(["IA", "Robótica", "Biotecnología", "Matemáticas"]),
            "autorId": autor_id
        }
        with self.client.post("/papers", json=payload, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    created = response.json().get("dato")
                    paper_id = created.get("id")
                    if paper_id:
                        self.paper_ids.append(paper_id)
                    response.success()
                except Exception as e:
                    response.failure(f"Error parsing paper: {e}")
            else:
                response.failure(f"Error creando paper: {response.text}")

    @task(2)
    def listar_papers(self):
        self.client.get("/papers")

    @task(1)
    def obtener_paper_por_id(self):
        if not self.paper_ids:
            return
        paper_id = random.choice(self.paper_ids)
        self.client.get(f"/papers/{paper_id}")

    @task(1)
    def actualizar_paper(self):
        if not self.paper_ids:
            return
        with self.autor_ids_lock:
            if not self.autor_ids:
                return
            autor_id = random.choice(self.autor_ids)
        paper_id = random.choice(self.paper_ids)
        fecha_pub = date.today() - timedelta(days=random.randint(0, 3650))
        payload = {
            "titulo": f"PaperActualizado {self.random_string(5)}",
            "anioPublicacion": random.randint(2000, 2025),
            "editorial": f"EditorialActualizada {self.random_string(4)}",
            "isbn": self.random_isbn(),
            "resumen": "Resumen actualizado generado por Locust",
            "orcid": self.random_orcid(),
            "fechaPublicacion": fecha_pub.isoformat(),
            "revista": random.choice(["Nature", "Science", "IEEE", "ACM"]),
            "areaInvestigacion": random.choice(["IA", "Robótica", "Biotecnología", "Matemáticas"]),
            "autorId": autor_id
        }
        self.client.put(f"/papers/{paper_id}", json=payload)

    @task(1)
    def eliminar_paper(self):
        if not self.paper_ids:
            return
        paper_id = self.paper_ids.pop()
        self.client.delete(f"/papers/{paper_id}")

    @task(2)
    def listar_notificaciones(self):
        self.client.get("/notificaciones")

    @task(2)
    def listar_catalogo(self):
        self.client.get("/catalogo")
