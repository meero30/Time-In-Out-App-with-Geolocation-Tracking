-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2023-07-24 01:36:56
-- 服务器版本： 10.4.28-MariaDB
-- PHP 版本： 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `scworker`
--

-- --------------------------------------------------------

--
-- 表的结构 `workers`
--

CREATE TABLE `workers` (
  `id` int(11) NOT NULL,
  `fullname` text NOT NULL,
  `email` varchar(20) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `password` varchar(255) NOT NULL,
  `apiKey` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `workers`
--

INSERT INTO `workers` (`id`, `fullname`, `email`, `phone`, `password`, `apiKey`) VALUES
(4, 'Xinlong Bao', 'hhwbxl1314@gmail.com', '09158071565', '$2y$10$C0jCDI/puhPMjfwsIBJnvemcsOss9VufzB8Jz5zr78C80jt0AT7Bm', ''),
(5, 'Raven Huang', 'example@gmail.com', '09158071566', '$2y$10$LCOvXWOY/Xzh8QwHARU8TeaTlG0yecZRTcfv5i.cQGnX/dkUYn4A2', '6f68536d7ecf2e08e1e23de9cdb097910b3c40dddb5cb0'),
(6, 'HanSong Bao', '329045212@qq.com', '15990297434', '$2y$10$DX2xmLSob1ra8KalooKqsuRgvfcJ11rxqDHdTk8mPN5P/Dl7lkDj6', '831b195682ef58b00894de13a4c6ea6e099b90ae504fa1');

--
-- 转储表的索引
--

--
-- 表的索引 `workers`
--
ALTER TABLE `workers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `workers`
--
ALTER TABLE `workers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
