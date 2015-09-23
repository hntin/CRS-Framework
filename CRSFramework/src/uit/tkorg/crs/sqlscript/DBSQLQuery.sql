// Select cac bai bao la cong tac cua 2 tac gia khac co quan.
// Kiem tra xem cac bai do co that su la du lieu cong tac khong?
// Phan tich xem cac co quan cua cac tac gia cong tac do ntn

// Lay ra nhung researchers cua NII
SELECT idOrg, orgName FROM org o where orgName Like '%National Institute of Informatics%';
// idOrg = '838'; NII
SELECT * FROM author a where idOrg = '838';

//CHON DANH SACH CAC BAI BAO MA TAC GIA CUA NO THUOC 2 TO CHUC (idORG) KHAC NHAU.
SELECT a.idAuthor, ap.idPaper
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
// Number of rows = 5681505

SELECT ap.idAuthor, ap.idPaper
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
// Number of rows = 5681505

SELECT ap.idPaper
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
GROUP BY ap.idPaper
// Number of rows = 2268321

SELECT ap.idAuthor, ap.idPaper
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
GROUP BY ap.idPaper
// Number of rows = 2268321

SELECT ap.idPaper
FROM Author_Paper ap, Author a 
WHERE ap.idAuthor = a.idAuthor
GROUP BY ap.idPaper
HAVING COUNT(DISTINCT a.idOrg) > 1
// Number of rows = 1024450

SELECT ap.idPaper
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
GROUP BY ap.idPaper
HAVING COUNT(DISTINCT a.idOrg) > 1
// Number of rows = 1024450

SELECT ap.idPaper
FROM Author_Paper ap, Author a
WHERE ap.idAuthor = a.idAuthor AND a.idOrg IS NOT NULL
GROUP BY ap.idPaper
HAVING COUNT(DISTINCT a.idOrg) > 1
// Number of rows = 1024450

SELECT ap.idPaper, a.idAuthor
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
WHERE a.idOrg IS NOT NULL
GROUP BY ap.idPaper
HAVING COUNT(DISTINCT a.idOrg) > 1
// Number of rows = 1024450

SELECT ap.idPaper, a.idAuthor, a.authorName, a.idOrg
FROM Author_Paper ap join Author a on ap.idAuthor = a.idAuthor
WHERE a.idOrg IS NOT NULL
GROUP BY ap.idPaper
HAVING COUNT(DISTINCT a.idOrg) > 1
// Number of rows = 1024450


// CAN LOC BO NHUNG TAC GIA MA 5, 10 NAM TINH DEN THOI DIEM HIEN TAI, CUOI CUNG TRAINING NET KHONG CO PAPER?

// LOC RA DANH SACH DONG TAC GIA CUA TUNG NGUOI?
SELECT DISTINCT p1.idAuthor
FROM author_paper p1
WHERE p1.idAuthor != 1 AND p1.idPaper IN (SELECT p2.idPaper FROM author_paper p2 WHERE p2.idAuthor = 1)
Order by p1.idAuthor

// DUA TREN DANH SACH DONG TAC GIA, LOC RA CAC TAC GIA KHAC INSTITUTE.

1. Loc ra danh sach dong tac gia cua mot tác gia cho truoc
select *
from Author_Paper ap
where ap.idAuthor = givenIdAuthor

-- Tat ca cac paper cua 1 tac gia cho truoc
select ap.idPaper
from Author_Paper ap
where ap.idAuthor = givenIdAuthor
-- Tat ca tac gia viet cac paper do
select ap.idAuthor
from Author_Paper ap
where ap.idAuthor <> givenIdAuthor 
    and ap.idPaper in ( select ap1.idPaper
                        from Author_Paper ap1
                        where ap1.idAuthor = givenIdAuthor)

						
2. Loc ra nhung nguoi dong tác gia mà khác co quan voi mot tác gia cho truoc 
select ap.idAuthor
from Author_Paper ap join Author a on a.idAuthor = ap.idAuthor
where a.idOrg <> givenIdOrg
    and ap.idPaper in ( select ap1.idPaper
                        from Author_Paper ap1
                        where ap1.idAuthor = givenIdAuthor)

3. Loc ra danh sách tat ca các tác gia la dong tác gia cua tat ca các tác gia trong DB.
-- Ket qua lon, co the tran du lieu
select distinct ap1.idAuthor, ap2.idAuthor
from Author_Paper ap1 join Author_Paper ap2 on ap1.idPaper = ap2.idPaper
order by ap1.idAuthor

4. Loc ra danh sách tat ca các tác gia là dong tác gia, nhung khác co quan voi tat ca các tác gia trong DB.
-- Ket qua lon, co the tran du lieu
select distinct ap1.idAuthor, ap2.idAuthor
from Author a1 join Author_Paper ap1 on a1.idAuthor = ap1.idAuthor
join Author_Paper ap2 on ap1.idPaper = ap2.idPaper
join Author a2 on a2.idAuthor = ap2.idAuthor
where a1.idOrg <> a2.idOrg -- chi xet author co idOrg khac null, neu muon xet null thi them: or a1.idOrg is null or a2.idOrg is null

SELECT LastName,FirstName,Address FROM Persons
WHERE Address IS NOT NULL

5. Loc ra nhung bai la cua 2 tac gia thuoc 2 co quan khac nhau trong nam publish sau cung.

SELECT ap.idAuthor, ap.idPaper, p.title, p.abstract FROM Author_Paper ap join Paper p on ap.idPaper = p.idPaper WHERE ap.idAuthor>300000 and ap.idAuthor <=400000 Order By idAuthor

----------------------------------------------------------------------
XAY DUNG DATASET CHO BAI BAO CRS-ISI-2015 (11/11/2014)

1. Loc danh sach bai bao, tac gia trong khoang thoi gian.
select paper.idPaper,author_paper.idAuthor
from paper,author_paper
where paper.idPaper = author_paper.idPaper and
			paper.year >= 2001 and paper.year <= 2003
			
2. Loc danh sach cac junior va senior:
Xet 2 khoang thoi gian [2001-2003], [2004-2006]
* Junior for training: 
- La nhung nguoi co it hon 3 bai bao, co dong tac gia, chua co trich dan trong [2001-2003], 
- Co them cac co-authorship moi trong [2004-2006].

* Senior for training: 
- Có tu 3 bai bao tro len, co dong tac gia va co trich dan [2001-2003]
- Co them cac co-authorship moi trong [2004-2006].

3. Loc ra cac mau (+), (-) cho junior.




