-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 05-05-2025 a las 21:43:58
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";



/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `biblioteca`
--
CREATE DATABASE IF NOT EXISTS `biblioteca` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `biblioteca`;


-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuracion_sistema`
--

CREATE TABLE `configuracion_sistema` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `maximo_ejemplares` int(11) DEFAULT 3,
  `mora_diaria` decimal(5,2) DEFAULT 1.00,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `configuracion_sistema`
--

INSERT INTO `configuracion_sistema` (`id`, `maximo_ejemplares`, `mora_diaria`) VALUES
(1, 2, 0.10);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones`
--

CREATE TABLE `devoluciones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_prestamo` int(11) NOT NULL, -- FK a prestamos.id
  `fecha_devolucion` date NOT NULL,
  `mora_pagada` decimal(10,2) DEFAULT NULL, -- Puede ser NULL
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `devoluciones`
--

INSERT INTO `devoluciones` (`id`, `id_prestamo`, `fecha_devolucion`, `mora_pagada`) VALUES
(1, 1, '2025-04-26', 0.00),
(2, 2, '2025-05-01', 0.00);

--
-- Disparadores `devoluciones`
--
DELIMITER $$
CREATE TRIGGER `tg_update_ejemplar_estado_disponible` AFTER INSERT ON `devoluciones` FOR EACH ROW begin 
declare id_ultimo_prestamo int;
set id_ultimo_prestamo = (select id_prestamo from devoluciones 
where id = (select max(id) from devoluciones));
update ejemplares set estado = "Disponible"
where id = (select id_ejemplar from prestamos where id = id_ultimo_prestamo);
end
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `documentos`
--

CREATE TABLE `documentos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `titulo` varchar(200) NOT NULL,
  `autor` varchar(150) DEFAULT NULL,
  `editorial` varchar(100) DEFAULT NULL,
  `anio_publicacion` int(11) DEFAULT NULL,
  `id_tipo_documento` int(11) NOT NULL, -- FK a tipo_documento.id
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `documentos`
--

INSERT INTO `documentos` (`id`, `titulo`, `autor`, `editorial`, `anio_publicacion`, `id_tipo_documento`) VALUES
(1, 'Fundamentos de Programación', 'José Ramírez', 'Alfaomega', 2018, 1),
(2, 'Revista Científica Don Bosco', 'Varios', 'Editorial Don Bosco', 2023, 2),
(3, 'Tesis sobre IA Educativa', 'Laura Sánchez', 'Universidad Don Bosco', 2021, 4),
(4, 'Obras Completas de Shakespeare', 'William Shakespeare', 'Planeta', 2015, 3),
(5, 'Curso de Java en CD', 'Fernando Núñez', 'Tecnología Ediciones', 2016, 5),
(6, 'Diario de 1992', 'Diario el mundo', 'Diario el mundo', 1992, 6);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ejemplares`
--

CREATE TABLE `ejemplares` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_documento` int(11) NOT NULL, -- FK a documentos.id
  `ubicacion` varchar(100) DEFAULT NULL,
  `estado` enum('Disponible','Prestado') DEFAULT 'Disponible',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


--
-- Volcado de datos para la tabla `ejemplares`
--

INSERT INTO `ejemplares` (`id`, `id_documento`, `ubicacion`, `estado`) VALUES
(1, 1, 'Estante A1 - Nivel 1', 'Disponible'),
(2, 1, 'Estante A1 - Nivel 1', 'Disponible'),
(3, 2, 'Estante B3 - Nivel 1', 'Disponible'),
(4, 3, 'Estante C2 - Nivel 3', 'Disponible'),
(5, 4, 'Estante D1 - Nivel 2', 'Disponible'),
(6, 4, 'Estante D1 - Nivel 2', 'Disponible'),
(7, 5, 'Estante E1 - Cajón Multimedia', 'Disponible'),
(8, 6, 'Estante F1 - Diarios', 'Disponible');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `prestamos`
CREATE TABLE IF NOT EXISTS prestamos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT NOT NULL,
    id_ejemplar     INT NOT NULL,
    fecha_prestamo  DATE NOT NULL,
    fecha_devolucion DATE DEFAULT NULL,
    fecha_limite    DATE NOT NULL,
    mora            DECIMAL(10,2) DEFAULT 0.00,
    
  -- ...constraints y indexes
    CONSTRAINT fk_pres_usuario
        FOREIGN KEY (id_usuario)  REFERENCES usuarios(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_pres_ejemplar
        FOREIGN KEY (id_ejemplar) REFERENCES ejemplares(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_pres_user_estado (id_usuario, fecha_devolucion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Volcado de datos para la tabla `prestamos`
--

INSERT INTO `prestamos` (`id`, `id_usuario`, `id_ejemplar`, `fecha_prestamo`, `fecha_devolucion`, `fecha_limite`, `mora`) VALUES
(1, 3, 1, '2025-04-20', NULL, '2025-04-27', 0.00),
(2, 3, 4, '2025-04-25', NULL, '2025-05-02', 0.00);

--
-- Disparadores `prestamos`
--
DROP TRIGGER IF EXISTS tg_update_ejemplar_estado_prestado;
DELIMITER $$
CREATE TRIGGER tg_update_ejemplar_estado_prestado
AFTER INSERT ON prestamos
FOR EACH ROW
BEGIN
    UPDATE ejemplares
    SET    estado = 'Prestado'
    WHERE  id = NEW.id_ejemplar;
END $$
DELIMITER ;


-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipo_documento`
--

CREATE TABLE `tipo_documento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tipo` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_tipo_documento` (`tipo`)
);
-- evitar duplicados en tipo_documento
ALTER TABLE tipo_documento
  ADD CONSTRAINT unq_tipo_documento UNIQUE (tipo);
--
-- Volcado de datos para la tabla `tipo_documento`
--

INSERT INTO `tipo_documento` (`id`, `tipo`) VALUES
(1, 'Libro'),
(2, 'Revista'),
(3, 'Obra'),
(4, 'Tesis'),
(5, 'CD'),
(6, 'Otro');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipo_usuario`
--

CREATE TABLE `tipo_usuario` (
  `id` int(11) NOT NULL,
  `tipo` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tipo_usuario`
--

INSERT INTO `tipo_usuario` (`id`, `tipo`) VALUES
(1, 'Administrador'),
(2, 'Profesor'),
(3, 'Alumno');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `id_tipo_usuario` int(11) NOT NULL,
  `estado` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `correo`, `contrasena`, `id_tipo_usuario`, `estado`) VALUES
(1, 'admin', 'admin@udb.com', 'AdminUdb2025.', 1, 1),
(2, 'Profesor1', 'profesor1@udb.com', 'ProfesorUDB2025.', 2, 1),
(3, 'Alumno1', 'alumno1@udb.com', 'AlumnoUDB2025.', 3, 1);

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `politicas_prestamo`
-- --------------------------------------------------------

CREATE TABLE `politicas_prestamo` (
  `id_politica` int(11) NOT NULL AUTO_INCREMENT,
  `id_tipo_usuario` int(11) NOT NULL,
  `max_ejemplares_prestamo` int(11) NOT NULL DEFAULT 1,
  `dias_prestamo_default` int(11) NOT NULL DEFAULT 7,
  PRIMARY KEY (`id_politica`),
  UNIQUE KEY `unq_politica_tipo_usuario` (`id_tipo_usuario`), -- Asegura una política por tipo de usuario
  CONSTRAINT `fk_politica_tipo_usuario` FOREIGN KEY (`id_tipo_usuario`) REFERENCES `tipo_usuario` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Volcado de datos inicial para la tabla `politicas_prestamo`---- 
INSERT INTO `politicas_prestamo` (`id_tipo_usuario`, `max_ejemplares_prestamo`, `dias_prestamo_default`) VALUES
(1, 10, 30), 
(2, 5, 15),  
(3, 3, 7);


-- -------------------------------------------------------------------
-- Estrucutra de la tabla de mora diaria por año --------------------------

CREATE TABLE IF NOT EXISTS mora_anual (
    anio        INT PRIMARY KEY,
    mora_diaria DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO mora_anual (anio, mora_diaria) VALUES (2025, 0.15);
-- --------------------------------------------

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `configuracion_sistema`
--
ALTER TABLE `configuracion_sistema`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_prestamo` (`id_prestamo`);

--
-- Indices de la tabla `documentos`
--
ALTER TABLE `documentos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_tipo_documento` (`id_tipo_documento`);

--
-- Indices de la tabla `ejemplares`
--
ALTER TABLE `ejemplares`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_documento` (`id_documento`) USING BTREE;

--
-- Indices de la tabla `prestamos`
--
ALTER TABLE `prestamos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_ejemplar` (`id_ejemplar`);

--
-- Indices de la tabla `tipo_documento`
--
ALTER TABLE `tipo_documento`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `tipo_usuario`
--
ALTER TABLE `tipo_usuario`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `correo` (`correo`),
  ADD KEY `id_tipo_usuario` (`id_tipo_usuario`) USING BTREE;

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `configuracion_sistema`
--
ALTER TABLE `configuracion_sistema`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `documentos`
--
ALTER TABLE `documentos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `ejemplares`
--
ALTER TABLE `ejemplares`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `prestamos`
--
ALTER TABLE `prestamos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `tipo_documento`
--
ALTER TABLE `tipo_documento`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `tipo_usuario`
--
ALTER TABLE `tipo_usuario`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD CONSTRAINT `devoluciones_ibfk_1` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamos` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `documentos`
--
ALTER TABLE `documentos`
  ADD CONSTRAINT `documentos_ibfk_1` FOREIGN KEY (`id_tipo_documento`) REFERENCES `tipo_documento` (`id`);

--
-- Filtros para la tabla `ejemplares`
--
ALTER TABLE `ejemplares`
  ADD CONSTRAINT `ejemplares_ibfk_1` FOREIGN KEY (`id_documento`) REFERENCES `documentos` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `prestamos`
--
ALTER TABLE `prestamos`
  ADD CONSTRAINT `prestamos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `prestamos_ibfk_2` FOREIGN KEY (`id_ejemplar`) REFERENCES `ejemplares` (`id`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`id_tipo_usuario`) REFERENCES `tipo_usuario` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
