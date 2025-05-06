-- consulta para buscar libros 
-- esta consulta le servira al alumno y al profesor
SELECT 
    d.titulo,
    d.autor,
    d.editorial,
    d.anio_publicacion,
    td.tipo,
    e.estado,
    COUNT(e.id) AS cantidad_ejemplares
FROM documentos d
INNER JOIN tipo_documento td ON td.id = d.id_tipo_documento
LEFT JOIN ejemplares e ON e.id_documento = d.id
where concat_ws(d.titulo,d.autor,d.editorial) like '%Alfa%'
GROUP BY 
    d.id, d.titulo, d.autor, d.editorial, d.anio_publicacion, td.tipo;
    
-- consulta saber el numero de prestamos que tiene un usuario
select u.nombre, u.correo, COUNT(p.id) as cantidad_prestamos from prestamos p
inner join usuarios u
on p.id_usuario = u.id
inner join ejemplares e
on p.id_ejemplar = e.id
where e.estado = "Prestado"
group by u.nombre, u.correo;

-- usuario con mora
select u.nombre, u.correo, p.fecha_prestamo, p.fecha_limite from prestamos p
inner join usuarios u
on p.id_usuario = u.id
inner join ejemplares e
on p.id_ejemplar = e.id
where e.estado = "Prestado" and p.fecha_limite < CURDATE()
and u.correo = "alumno1@udb.com";

-- consular para lista de usuarios con mora
select u.nombre, u.correo, p.fecha_prestamo, p.fecha_limite from prestamos p
inner join usuarios u
on p.id_usuario = u.id
inner join ejemplares e
on p.id_ejemplar = e.id
where e.estado = "Prestado" and p.fecha_limite < CURDATE();

-- consulta para obtener prestamos activos  
select u.nombre, u.correo, d.titulo, d.autor, p.fecha_prestamo, p.fecha_limite from prestamos p
inner join ejemplares e
on p.id_ejemplar = e.id
inner join documentos d
on e.id_documento = d.id
inner join usuarios u
on p.id_usuario = u.id
where e.estado = "Prestado";

-- consulta para ver el historial de prestamos
select u.nombre, u.correo, d.titulo, d.autor, p.fecha_prestamo from prestamos p
inner join usuarios u
on p.id_usuario = u.id
inner join ejemplares e
on p.id_ejemplar = e.id
inner join documentos d
on  e.id_documento = d.id;

-- consulta para inicio de sesion 
select u.nombre, u.correo, tu.tipo from usuarios u
inner join tipo_usuario tu
on u.id_tipo_usuario = tu.id
where u.correo = "admin@udb.com" and u.contrasena = "AdminUdb2025.";

-- flujo de consultas para cambiar contraseña 

-- paso 1 verificar si correo existe
select * from usuarios u
where u.correo = "admin@udb.com";

-- paso 2 se debe verificar por codigo que estara fijo en el codigo

-- paso 3 si el codigo es correcto se procede actualizar la contrasena
update usuarios set contrasena = "nuevaContrasena"
where correo = "admin@udb.com";

-- historial de devoluciones
select u.nombre, u.correo, d.titulo, p.fecha_prestamo, dv.fecha_devolucion, dv.mora_pagada from devoluciones dv
inner join prestamos p
on dv.id_prestamo = p.id
inner join usuarios u
on p.id_usuario = u.id
inner join ejemplares e
on p.id_ejemplar = e.id
inner join documentos d
on e.id_documento = d.id;





